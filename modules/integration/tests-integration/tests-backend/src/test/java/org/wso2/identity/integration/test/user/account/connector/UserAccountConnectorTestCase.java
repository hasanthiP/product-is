/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.identity.integration.test.user.account.connector;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.clients.user.account.connector.UserAccountConnectorServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class UserAccountConnectorTestCase extends ISIntegrationTest {

    private UserAccountConnectorServiceClient serviceClient;
    private final static String ADMIN_USER = "admin";
    private final static String USER_1 = "testuser11@carbon.super";
    private final static String USER_PASSWORD_1 = "testuser11";
    private final static String USER_2 = "testuser11@wso2.com";
    private final static String USER_PASSWORD_2 = "testuser11";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem
                (null, null);
        serviceClient = new UserAccountConnectorServiceClient(sessionCookie, backendURL, configContext);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        serviceClient = null;
    }

    @Test(alwaysRun = true, description = "Test create user account associations")
    public void testConnectUserAccount() throws Exception {

        // Create associations
        serviceClient.connectUserAccount(USER_1, USER_PASSWORD_1);
        serviceClient.connectUserAccount(USER_2, USER_PASSWORD_2);

        // Retrieve associations
        String [] associations = serviceClient.getConnectedAccountsOfUser();

        Assert.assertTrue(associations != null && associations.length > 0, "Unable to create user account association" +
                                                                           " for user" );
        Assert.assertTrue(isAssociationAvailable(associations, USER_1), "Unable to create user association with a " +
                                                                        "super tenant user");
        Assert.assertTrue(isAssociationAvailable(associations, USER_2), "Unable to create user association with a " +
                                                                        "tenant user");

    }

    @Test(alwaysRun = true, description = "Test switch user logged in account ",
          dependsOnMethods = { "testConnectUserAccount" })
    public void switchLoggedInUser() throws Exception {

        serviceClient.switchLoggedInUser(USER_1);

        String [] associations = serviceClient.getConnectedAccountsOfUser();

        Assert.assertTrue(isAccountSwitched(associations, USER_1), "Unable to switch user to a super tenant " +
                                                                         "user");

        serviceClient.switchLoggedInUser(USER_2);

        associations = serviceClient.getConnectedAccountsOfUser();

        Assert.assertTrue(isAccountSwitched(associations, USER_2), "Unable to switch user to a tenant user");

        serviceClient.switchLoggedInUser(ADMIN_USER);
    }

    @Test(alwaysRun = true, description = "Test delete user account association",
          dependsOnMethods = { "switchLoggedInUser" })
    public void testDeleteUserAccountConnection() throws Exception {

        serviceClient.deleteUserAccountConnection(USER_1);

        String [] associations = serviceClient.getConnectedAccountsOfUser();

        Assert.assertFalse(isAssociationAvailable(associations, USER_1), "Unable to delete user association of a " +
                                                                         "super tenant user");

        serviceClient.deleteUserAccountConnection(USER_2);

        associations = serviceClient.getConnectedAccountsOfUser();

        Assert.assertFalse(isAssociationAvailable(associations, USER_2),  "Unable to delete user association of a " +
                                                                          "tenant user");

    }

    private boolean isAssociationAvailable(String [] associations, String userName){
        if(associations != null){
            for(String association : associations){
                if(userName.equals(association)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAccountSwitched(String [] associations, String userName){
        if(associations != null && associations.length > 0){
            for(String association : associations){
                if(userName.equals(association)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
