package io.infrastructor.core.inventory.aws;

import org.junit.Test
import org.junit.experimental.categories.Category
import io.infrastructor.core.utils.AmazonRoute53Utils

import static io.infrastructor.core.inventory.aws.ManagedAwsInventory.managedAwsInventory
import static io.infrastructor.core.inventory.aws.AwsInventory.awsInventory

@Category(AwsCategory.class)
public class AwsRoute53Test extends AwsTestBase {
    
    @Test
    public void findAwsNodes() {
        try {
            def inventory = managedAwsInventory(AWS_ACCESS_KEY_ID, AWS_SECRET_KEY, AWS_REGION) {
                managedZone(parallel: 2, tags: [managed: true]) {
                    ec2 {
                        name = 'simple-y'
                        imageId = 'ami-3f1bd150' // Ubuntu Server 16.04 LTS (HVM), SSD Volume Type
                        instanceType = 't2.micro'
                        subnetId = 'subnet-fd7b3b95' // EU Centra-1, default VPC with public IPs
                        keyName = 'aws_infrastructor_ci'
                        securityGroupIds = ['sg-8e6fcae5'] // default-ssh-only
                        
                        username = "ubuntu"
                        keyfile = "resources/aws/aws_infrastructor_ci"
                        usePublicIp = true
                    }
                    
                    ec2 {
                        name = 'simple-x'
                        imageId = 'ami-3f1bd150' // Ubuntu Server 16.04 LTS (HVM), SSD Volume Type
                        instanceType = 't2.micro'
                        subnetId = 'subnet-fd7b3b95' // EU Centra-1, default VPC with public IPs
                        keyName = 'aws_infrastructor_ci'
                        securityGroupIds = ['sg-8e6fcae5'] // default-ssh-only
                    
                        username = "ubuntu"
                        keyfile = "resources/aws/aws_infrastructor_ci"
                        usePublicIp = true
                    }
                }
                
                route53(hostedZoneId: 'Z36BEXFJC6IRSG') {
                    dns(name: 'simple.test.internal', ttl: 500, resources: {'managed:true'}, type: 'A')
                }
                
            }
            
            inventory.setup {}
            
            def nodes = inventory.getManagedNodes()
            
            assert nodes
            assert nodes.size() == 2
            
            def amazonRoute53 = AmazonRoute53Utils.amazonRoute53(AWS_ACCESS_KEY_ID, AWS_SECRET_KEY, AWS_REGION)
            def recordSet = AmazonRoute53Utils.findDnsRecordSet(amazonRoute53, 'Z36BEXFJC6IRSG', 'simple.test.internal')
            
            assert recordSet
            assert recordSet.records.size() == 2
            assert recordSet.records.contains(nodes[0].privateIp)
            assert recordSet.records.contains(nodes[1].privateIp) 
            
        } finally {
            managedAwsInventory(AWS_ACCESS_KEY_ID, AWS_SECRET_KEY, AWS_REGION) { 
                managedZone(tags: [managed: true]) {} 
                
                route53(hostedZoneId: 'Z36BEXFJC6IRSG') {
                    dns(name: 'simple.test.internal', ttl: 500, resources: {'managed:true'}, type: 'A')
                }
                
            }.setup {}
        }
    }
}
