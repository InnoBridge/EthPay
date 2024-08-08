# EthPay

## Get your MongoDB URI
Go back to your database on MongoDB Atlas, click on Connect button
![alt text](./images/MongoAtlasConnect.png)

Click on “Connect to your application”
![alt text](./images/ConnectToApplication.png)

Copy the MongoDB URI and fill in the <username> and <password> with the username and password you saved before
![alt text](./images/MongoDBURI.png)

## Developing Locally in Docker
### Setting up Environment Variables
Run the following command to create a .env file which will initialize the environment variables
```bash
cp .env.example .env
```
In the .env file fill in the following environment variables
```text
MONGO_DATABASE_URI=<Your MongoDB URI>
```

### Running Docker Compose
In the current workspace run the following in your terminal initialize Docker swam
```bash
docker swarm init
```

Run the following command to build and setup Docker compose
```bash
docker-compose build
```

Run the following command to start the Docker compose
```bash
docker-compose up
```

### Configure IntelliJ IDEA for Remote Debugging in Docker
Open IntelliJ IDEA.
1. Go to Run > Edit Configurations.
2. Click the + icon and select Remote JVM Debug.
3. Set the following configuration:
   - Name: Remote Debug
   - Host: localhost
   - Port: 5005
4. Click Apply and OK.
![alt text](./images/RemoteDebugging.png)

Run the following command to ssh into your Docker container
```bash
docker exec -it ethpay-application sh
```

Once you are in the Docker container run the 
following command to run your application in debug mode
```bash
./debug.sh
```

Then in Intellij Idea 
- Go to Run > Debug 'Remote Debug'

## Deploy to Render
### Setting up Environment Variables
Go to your Java application on https://dashboard.render.com/ , in Environment. Add the Environmental Variable, with the key MONGO_DATABASE_URI.
![alt text](./images/RenderEnvVariables.png)

### Whitelist Application’s IP Address
Whitelisting our Spring Boot application’s IP Address on MongoDB Atlas adds an extra layer of security, because we only want our Spring Boot application to access our database and don’t want to allow public access to our database.
On https://dashboard.render.com/ click on Connect to obtain the set of Static Outbound IP Addresses.
![alt text](./images/RenderStaticOutboundIP.png)

No go back to your database on MongoDB Atlas, click on the sidebar tab Network Access and add all the Static Outbound IP Addresses.
![alt_test](./images/MongoDBNetworkAccess.png)

Push your code to the main branch of your Spring Boot GitHub repo. When we integrate our GitHub repo with Render as described in the previous tutorial, the default setup triggers a build every time we push code to the main branch.

After the deployment has completed you can execute your api on 
```text
https://<application url>/swagger-ui/index.html.
```
