import xml.etree.ElementTree as ET
import glob

print("Adding Spring Boot DevTools to POM files...")
pom_files = glob.glob(r'd:\Desktop\Leave-management\*\pom.xml')
ns = {'mvn': 'http://maven.apache.org/POM/4.0.0'}

for pom in pom_files:
    ET.register_namespace('', "http://maven.apache.org/POM/4.0.0")
    tree = ET.parse(pom)
    root = tree.getroot()
    
    dependencies_elem = root.find('mvn:dependencies', ns)
    if dependencies_elem is not None:
        # Check if already present
        present = False
        for dep in dependencies_elem.findall('mvn:dependency', ns):
            artifact_id = dep.find('mvn:artifactId', ns)
            if artifact_id is not None and artifact_id.text == 'spring-boot-devtools':
                present = True
                break
        
        if not present:
            # Create dependency element
            new_dep = ET.Element('dependency')
            
            group_id = ET.SubElement(new_dep, 'groupId')
            group_id.text = 'org.springframework.boot'
            
            art_id = ET.SubElement(new_dep, 'artifactId')
            art_id.text = 'spring-boot-devtools'
            
            scope = ET.SubElement(new_dep, 'scope')
            scope.text = 'runtime'
            
            optional = ET.SubElement(new_dep, 'optional')
            optional.text = 'true'
            
            dependencies_elem.append(new_dep)
            tree.write(pom, encoding='utf-8', xml_declaration=True)
            print(f"Added devtools to {pom}")
