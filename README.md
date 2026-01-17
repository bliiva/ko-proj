## Kombinatoriālās optimizācijas projekts lidostas darbībai



1. Start java thingy:  
`ko-proj` folder: `mvn clean spring-boot:run`
2. Call test example:
`call_test.py`
3. Check http://localhost:8080/index.html, click on returned hash to see score


## Publishing Docker Image to Docker Hub

1. **Build JAR:**
   ```sh
   mvn clean package
   ```
   This will create a JAR file in the `target/` directory.

2. **Build the Docker image:**
   ```sh
   docker build -t <dockerhub-username>/ko-proj .
   ```
   Replace `<dockerhub-username>` with your Docker Hub username.

3. **Log in to Docker Hub:**
   ```sh
   docker login
   ```
   Enter Docker Hub credentials when prompted.

4. **Push the image to Docker Hub:**
   ```sh
   docker push <dockerhub-username>/ko-proj
   ```

5. **Pull and run the image elsewhere:**
   ```sh
   docker pull <dockerhub-username>/ko-proj
   docker run -p 8080:8080 <dockerhub-username>/ko-proj
   ```
---
* If frontend does not reset, "hard-reset" it with `Ctrl + Shift + R`