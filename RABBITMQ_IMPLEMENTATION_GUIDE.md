# RabbitMQ Integration Guide: Leave Management System

## 1. The Core Problem: Synchronous vs Asynchronous
Before we introduced RabbitMQ, the **Admin Service** communicated with the **Leave Service** and **Timesheet Service** synchronously using OpenFeign. 

**The Synchronous Problem:**
If a Manager clicked "Approve Timesheet", the Admin Service would send an HTTP POST request to the Timesheet Service. The Admin Service then had to physically *wait* doing nothing until the Timesheet Service finished updating its database. If the Timesheet Service was slow or down, the Admin Service would timeout, crash, or make the user wait too long.

**The Asynchronous Solution (RabbitMQ):**
RabbitMQ is a "Message Broker"—like a highly efficient digital post office. Now, when a Manager clicks "Approve", the Admin Service just writes a message (event) and hands it directly to the RabbitMQ Post Office. The Admin Service instantly replies to the user, saying "Success!" without waiting. Meanwhile, RabbitMQ safely delivers that message to the Timesheet Service whenever it is ready to process it in the background.

---

## 2. What We Actually Built (The Architecture)

We built an **Event-Driven Architecture** utilizing the **Publish-Subscribe (Pub/Sub)** model.

1. **The Producer (Admin Service)**: Responsible for publishing the Approval/Rejection events to RabbitMQ.
2. **The Exchange (`leave-management-exchange`)**: Think of this as the central sorting facility. The Admin Service throws *all* messages here.
3. **The Queues (`timesheet-approval-queue` & `leave-approval-queue`)**: These are the mailboxes. The sorting facility routes Timesheet messages to the Timesheet Queue, and Leave messages to the Leave Queue.
4. **The Consumers (Timesheet & Leave Services)**: These services sit silently in the background and continuously pull mail out of their respective Queues to update their local databases.

---

## 3. Step-by-Step Code Review

### Step A: The Dependencies
We injected `spring-boot-starter-amqp` into the `pom.xml` of all 3 microservices. AMQP (Advanced Message Queuing Protocol) is the engine that allows Spring Boot to natively speak to RabbitMQ.

### Step B: The Configuration (Routing the Mail)
In the **Admin Service**, we created `RabbitMQConfig.java` to define the infrastructure.
```java
@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "leave-management-exchange";
    
    // We define the Exchange (The Sorting Facility)
    @Bean
    public TopicExchange exchange() { return new TopicExchange(EXCHANGE); }

    // We define the Queues (The Mailboxes)
    @Bean
    public Queue timesheetQueue() { return new Queue("timesheet-approval-queue"); }

    // We Bind them together using "Routing Keys" (The Zip Codes)
    @Bean
    public Binding bindingTimesheet(Queue timesheetQueue, TopicExchange exchange) {
        return BindingBuilder.bind(timesheetQueue).to(exchange).with("routing.timesheet.approve");
    }

    // Crucial: Converts our Java Objects into JSON so they can travel over the wire!
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
```

### Step C: The Publisher (Admin Service)
We refactored `AdminApprovalService.java` to stop using Feign, and start using `RabbitTemplate`.
```java
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public Map<String, Object> approveTimesheet(Long id, String comments) {
        // 1. Package the mail (The Payload)
        Map<String, Object> payload = Map.of("id", id, "status", "APPROVED", "comments", comments);
        
        // 2. Hand it to the post office!
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "routing.timesheet.approve", payload);
        
        // 3. Immediately reply to the user!
        return Map.of("message", "Action requested asynchronously.");
    }
```

### Step D: The Consumer (Timesheet Service)
In the Timesheet Service, we created `TimesheetApprovalListener.java` to automatically fire whenever a new message lands in its mailbox.
```java
@Component
public class TimesheetApprovalListener {

    @Autowired
    private TimesheetService timesheetService;

    // This annotation tells Spring to continuously listen to this specific queue!
    @RabbitListener(queues = "timesheet-approval-queue")
    public void handleTimesheetApproval(Map<String, Object> payload) {
        // 1. Extract the JSON payload components
        Long id = Long.valueOf(payload.get("id").toString());
        String status = (String) payload.get("status");
        
        // 2. Preform the heavy database update in the quiet background!
        timesheetService.updateStatus(id, status, ...);
    }
}
```

---

## 4. How to Test and See it working!

1. **Verify RabbitMQ is Running:**
   Open your browser to the RabbitMQ Dashboard: `http://localhost:15672/`
   Login with username: `guest` / password: `guest`.
   If you click the "Queues" tab, you will actively see `timesheet-approval-queue` existing with 0 messages waiting!

2. **Trigger the Code:**
   Open Swagger, generate a token, and use the **Admin Service** to hit `PUT /api/admin/approvals/timesheets/1?status=APPROVED`.

3. **Watch the Magic:**
   Instead of a slow, hanging loading bar, you will instantly get a 200 OK Response. 
   If you watch your IDE console logs for the **Timesheet Service**, you will instantly see it print:
   `>>> [RabbitMQ CONSUMER] Processing Async Timesheet Approval | ID: 1`

You have just witnessed true, highly-scalable, non-blocking Event-Driven Architecture!
