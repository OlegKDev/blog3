## REST API for Blog app

Technologies: Java 18, Spring 3, MySQL, H2, JUnit

- Implemented entities, repository, service and controller layers, custom runtime exceptions and controller for the exception handling; implemented DTOs with validation annotations
- Secured the application with JWT and role-based approach (Admin and User)
- Created the initialization SQL script for testing
- Developed the positive and negative unit tests for the repository, service, and controller layers, including JWT and role-based authorization


```bash
cd blog3/

# Run all tests
mvn clean test

# Run a single test
mvn clean -Dtest=TestClass test
mvn clean -Dtest=TestClass#testMethod test

# Build
mvn clean install 

# Build, skip tests
mvn clean install -Dmaven.test.skip=true

# To change `test` to `dev` profile
sed -i -e 's/test/dev/g' src/main/resources/application.properties
cat src/main/resources/application.properties
```

```txt
[INFO] 
[INFO] Results:
[INFO]
[INFO] Tests run: 73, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:20 min
[INFO] Finished at: 2023-02-24T18:30:05+02:00
[INFO] ------------------------------------------------------------------------
```

### Post Resource
<table>
	<tr>
		<th>HTTP Method</th><th>URL Path</th>
		<th>Status Code</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>POST</td>
		<td>/api/v1/posts</td>
		<td>201 Created</td>
		<td>Create a new post</td>
	</tr>
	<tr>
		<td>GET</td>
		<td>/api/v1/posts/{id}</td>
		<td>200 Ok</td>
		<td>Retrieve a post by id</td>
	</tr>
	<tr>
		<td>GET</td>
		<td>/api/v1/posts</td>
		<td>200 Ok</td>
		<td>Retrieve all the posts</td>
	</tr>
	<tr>
		<td>GET</td>
		<td>/api/v1/posts<br>?pageSize=10&pageNo=0<br>&sortingBy=id&sortingDir=asc</td>
		<td>200 Ok</td>
		<td>Retrieve the posts with pagination</td>
	</tr>
	<tr>
		<td>PUT</td>
		<td>/api/v1/posts/{id}</td>
		<td>200 Ok</td>
		<td>Update a post</td>
	</tr>
	<tr>
		<td>PATCH</td>
		<td>/api/v1/posts/{id}</td>
		<td>200 Ok</td>
		<td>Partially update a post</td>
	</tr>
	<tr>
		<td>DELETE</td>
		<td>/api/v1/posts/{id}</td>
		<td>200 Ok</td>
		<td>Remove a post</td>
	</tr>
</table>

### Comment Resource

<table>
	<tr>
		<th>HTTP Method</th>
		<th>URL Path</th>
		<th>Status Code</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>POST</td>
		<td>/api/v1/posts/{postId}/comments</td>
		<td>201 Created</td>
		<td>Create a new comment</td>
	</tr>
	<tr>
		<td>GET</td>
		<td>/api/v1/posts/{postId}/comments/{commentId}</td>
		<td>200 Ok</td>
		<td>Retrieve a comment by id</td>
	</tr>
	<tr>
		<td>GET</td>
		<td>/api/v1/posts/{postId}/comments</td>
		<td>200 Ok</td>
		<td>Retrieve the comments of the post</td>
	</tr>
	<tr>
		<td>PUT</td>
		<td>/api/v1/posts/{postId}/comments/{commentId}</td>
		<td>200 Ok</td>
		<td>Update a comment</td>
	</tr>
	<tr>
		<td>DELETE</td>
		<td>/api/v1/posts/{postId}/comments/{commentId}</td>
		<td>200 Ok</td>
		<td>Remove a comment</td>
	</tr>
	<tr>
		<td>DELETE</td>
		<td>/api/v1/posts/{postId}/comments</td>
		<td>200 Ok</td>
		<td>Remove all the comments of the post</td>
	</tr>
</table>

### Signup/Login

<table>
	<tr>
		<th>HTTP Method</th>
		<th>URL Path</th>
		<th>Status Code</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>POST</td>
		<td>/api/v1/auth/signup</td>
		<td>200 Ok</td>
		<td>Create a new user</td>
	</tr>
	<tr>
		<td>POST</td>
		<td>/api/v1/auth/login</td>
		<td>200 Ok</td>
		<td>Login a user</td>
	</tr>
</table>
