package io.infrastructor.core.processing.actions

import org.junit.Test

public class UploadTest extends ActionTestBase {
    
    @Test
    public void uploadAFileToRemoteHost() {
        inventory.provision {
            task filter: {'as:root'}, actions: {
                user  name: 'test'
                group name: 'testgroup'
                
                upload {
                    source = 'build/resources/test/fileupload.txt'
                    target = '/fileupload.txt'
                    owner = 'test'
                    group = 'testgroup'
                }
                
                assert shell("ls -alh /fileupload.txt").output.find(/test testgroup/)
                assert shell("cat /fileupload.txt").output.find(/simple/)
            }
        }
    } 
    
    @Test
    public void uploadAFileToADeepFolder() {
        inventory.provision {
            task filter: {'as:root'}, actions: {
                user  name: 'test'
                group name: 'testgroup'
                
                upload {
                    source = 'build/resources/test/fileupload.txt'
                    target = '/etc/deep/deep/unknown/folder/fileupload.txt'
                    owner = 'test'
                    group = 'testgroup'
                }
                
                assert shell("ls -alh /etc/deep/deep/unknown/folder/fileupload.txt").output.find(/test testgroup/)
                assert shell("cat /etc/deep/deep/unknown/folder/fileupload.txt").output.find(/simple/)
            }
        }
    } 
    
    @Test
    public void uploadAFileToRemoteHostWithoutPermissions() {
        inventory.provision {
            task filter: {'as:devops'}, actions: {
                def result = upload {
                    source = 'build/resources/test/fileupload.txt'
                    target = '/fileupload.txt'
                }
                
                assert result.exitcode != 0
            }
        }
    }
    
    @Test
    public void uploadAFileToRemoteHostWithSudo() {
        inventory.provision {
            task filter: {'as:devops'}, actions: {
                def result = upload {
                    sudo = true
                    source = 'build/resources/test/fileupload.txt'
                    target = '/fileupload.txt'
                }
                
                assert result.exitcode == 0
                assert shell("cat /fileupload.txt").output.find(/simple/)
            }
        }
    }
    
    @Test
    public void uploadFileWithUnknownOwner() {
        inventory.provision {
            task filter: {'as:root'}, actions: {
                // execute
                def result = upload source: 'build/resources/test/fileupload.txt', target: '/tmp/simple.txt', owner: 'doesnotexist'
                
                // assert
                assert result.exitcode != 0
            }
        }
    }
 
    @Test
    public void uploadFileWithUnknownGroup() {
        inventory.provision {
            task filter: {'as:root'}, actions: {
                // execute
                def result = upload source: 'build/resources/test/fileupload.txt', target: '/tmp/simple.txt', group: 'doesnotexist'
                
                // assert
                assert result.exitcode != 0
            }
        }
    }
    
    @Test
    public void uploadFileWithInvalidMode() {
        inventory.provision {
            task filter: {'as:root'}, actions: {
                // execute
                def result = upload source: 'build/resources/test/fileupload.txt', target: '/tmp/simple.txt', mode: '8888'
                
                // assert
                assert result.exitcode != 0
            }
        }
    }
    
    
    @Test
    public void decryptAndUploadFileToRemoteHost() {
        inventory.provision {
            task filter: {'as:devops'}, actions: {
                def result = upload {
                    sudo = true
                    source = 'build/resources/test/encrypted_full.tmpl'
                    target = '/fileupload.txt'
                    decryptionKey = 'secret'
                }
                
                assert result.exitcode == 0
                assert shell("cat /fileupload.txt").output.find(/secret message/)
            }
        }
    }
}
