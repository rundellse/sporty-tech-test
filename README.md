A repository containing a completion of the Sporty / Pleased take-home task.

---
Setup and Test:

Requirements:
 - Docker CLI  
 - OpenJDK 21 (or later)

(These steps tested in Ubuntu, other OS may need slight changes)  
1. Clone the repo into your local environment:
   
$ git clone https://github.com/rundellse/sporty-tech-test.git

3. Build the Application JAR
   
Navigate to the project root (e.g. /sporty-tech-test), then run:  
$ ./mvnw clean package

This creates a JAR file in the target directory.

3. Build the Docker Image
   
From the project root run:  
$ docker build -t sportytechtest .  
(If you see a permission denied error run with sudo or (preferably) create a docker group: https://docs.docker.com/engine/install/linux-postinstall/#manage-docker-as-a-non-root-user)

5. Run the Docker Container
   
Start on port 8080:  
$ docker run -p 8080:8080 sportytechtest

Your Spring Boot app is now running inside Docker and accessible at http://localhost:8080.

6. Test the API Endpoints
   
You can use curl, Postman or another API testing software of your choice to test your endpoints. Examples in curl.

Example: Create a Ticket  
$ curl -X POST http://localhost:8080/tickets \  
    -H "Content-Type: application/json" \  
    -d '{"userId":"user-001","subject":"Payment issue","description":"I was charged twice for the same order."}'  

Example: List Tickets  
$ curl "http://localhost:8080/tickets?userId=user-001"

Example: Update Ticket Status  
curl -X PATCH http://localhost:8080/tickets/{ticketId}/status \  
  -H "Content-Type: application/json" \  
  -d '{"status":"IN_PROGRESS"}'  
  
(Note: ticketId should be fetched from a previous ticket creation)

Example: Add Comment  
curl -X POST http://localhost:8080/tickets/{ticketId}/comments \  
  -H "Content-Type: application/json" \  
  -d '{"authorId":"agent-123","content":"We are investigating.","visibility":"PUBLIC"}'  
  
(Note: ticketId should be fetched from a previous ticket creation)  

7. Stopping the Container
   
To stop the running container, press Ctrl+C in the terminal where it’s running, or find the container ID with:  
$ docker ps

and stop it with:  
$ docker stop <container_id>  

8. Clean Up
   
Remove unused containers and images if needed:  
docker system prune

---
Design Assumptions / Followup:  
 - There is no specific definition for how 'assignment' is done for a given Ticket. This would presumably need to be added in future.
 - Status of Tickets can only go forwards or back in sequence (except for Closed which can only re-open). Not currently configurable.
 - Validating author type through having 'agent-' / 'user-' prepended in the userid is not sufficient. But it's all we have for now.
 - See TODO in TicketService: only public comments should be returned to users but we do not currently have authentication on request source.

---
AI Usage - All Github Copilot (GPT 4.1):  
 - Creating templates for the defined REST APIs, manually updated into final state with some iteration as the requirements were probed.
 - Creating templates for test cases, then expanded/duplicated and modified to expand coverage.
 - Writing the Dockerfile, few changes needed.
 - Writing the guide above, which I then went through to test and tweak as needed.
