# TextCompactor

A Spring Boot application for compressing text and files with Huffman coding. Use the built-in browser interface or the REST API to encode text, save and manage text-compression history, and compress or decompress files.

## Features

- **Text compression:** Encodes text with Huffman coding and returns the encoded bit string and its symbol-to-code table.
- **Saved text history:** Persists each text request, original text, encoded bits, and code table in MySQL.
- **File compression:** Accepts a file and returns a binary `.huff` archive.
- **File decompression:** Accepts an archive produced by this application and returns the original bytes.
- **Browser interface:** Provides the UI and file operations from the same Spring Boot server.
- Handles empty input, repeated-symbol input, and Unicode text.

Compression is data-dependent: Huffman encoding may not make every input smaller, particularly when archive or code-table overhead is significant.

## Requirements

- Java 17 or later
- MySQL 8 or a compatible MySQL server
- No separate Maven installation is needed; the project includes the Maven Wrapper.

## Run locally

1. Create a database:

   ```sql
   CREATE DATABASE textcompactor;
   ```

2. Configure the database connection. Set these environment variables before starting the application, replacing the example values with your local MySQL settings.

   **PowerShell**

   ```powershell
   $env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/textcompactor"
   $env:SPRING_DATASOURCE_USERNAME = "your_mysql_username"
   $env:SPRING_DATASOURCE_PASSWORD = "your_mysql_password"
   ```

   **macOS/Linux**

   ```bash
   export SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/textcompactor'
   export SPRING_DATASOURCE_USERNAME='your_mysql_username'
   export SPRING_DATASOURCE_PASSWORD='your_mysql_password'
   ```

   Keep credentials out of source control. The application uses Spring Data JPA to create or update the `text_requests` table.

3. Start the server from the `textcompressor` directory:

   **Windows**

   ```powershell
   cd textcompressor
   .\mvnw.cmd spring-boot:run
   ```

   **macOS/Linux**

   ```bash
   cd textcompressor
   ./mvnw spring-boot:run
   ```

4. Open [http://localhost:8080](http://localhost:8080) in your browser.

## REST API

The API is served from the same host and port as the browser interface.

### Compress text

`POST /api/text/compress`

Send JSON:

```http
Content-Type: application/json
```

```json
{
  "text": "Huffman coding compresses repeated symbols."
}
```

Or send a plain-text request body with `Content-Type: text/plain`.

The response is the saved text request, including its generated `id`, original `text`, `encodedText` bit string, and `codeTable`. The code table maps Unicode code points to bit codes and is needed to decode the encoded text.

```powershell
Invoke-RestMethod `
  -Uri http://localhost:8080/api/text/compress `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"text":"Huffman coding compresses repeated symbols."}'
```

### List text requests

`GET /api/text/requests`

Returns all saved text-compression requests in ascending ID order.

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/text/requests
```

### Delete a text request

`DELETE /api/text/requests/{id}`

Returns `204 No Content` when deleted, or `404 Not Found` if the ID does not exist.

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/text/requests/1 -Method Delete
```

### Compress a file

`POST /api/files/compress`

Send `multipart/form-data` with a file field named `file`. The response is a downloadable `compressed.huff` archive and includes `X-Original-Size` and `X-Compressed-Size` headers.

```bash
curl -X POST http://localhost:8080/api/files/compress \
  -F "file=@document.txt" \
  --output compressed.huff
```

### Decompress a file

`POST /api/files/decompress`

Send an application-generated archive as `multipart/form-data` in a field named `file`. The response is the original file bytes, downloaded as `decompressed.txt`.

```bash
curl -X POST http://localhost:8080/api/files/decompress \
  -F "file=@compressed.huff" \
  --output decompressed.txt
```

The server allows uploads up to 25 MB per file and 30 MB per request. Invalid or unsupported archives return `400 Bad Request`.

## Run tests

From the `textcompressor` directory:

**Windows**

```powershell
.\mvnw.cmd test
```

**macOS/Linux**

```bash
./mvnw test
```

The tests cover text encoding/decoding, file round trips, empty and single-symbol inputs, and invalid archive handling.

## Project layout

```text
textcompressor/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/example/textcompressor/
    │   │   ├── controller/   # Text and file REST endpoints
    │   │   ├── model/        # Persisted text request
    │   │   ├── repository/   # Spring Data JPA repository
    │   │   ├── service/      # Text encoding and history
    │   │   └── util/         # Huffman text and file codecs
    │   └── resources/
    │       ├── application.properties
    │       └── static/index.html
    └── test/
```

## Implementation notes

- Text requests are stored in MySQL, including the original text. Do not submit confidential text unless storing it in this database is appropriate.
- File compression and decompression return data directly; they are not added to the text request history.
- File archives use this application's Huffman file format and are intended to be decompressed by this application.
- The database connection is required for application startup, including when using the browser UI or file endpoints.
