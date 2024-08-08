# EthPay


## Developing in Docker
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