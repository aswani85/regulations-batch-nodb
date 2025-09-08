
# Regulations Batch (no DB) - commentsPdfJob

Features:
- commentsPdfJob reads comments for a given docketId (optionally startCommentId)
- generates PDF per comment and uploads PDF + attachments to SharePoint
- retry/backoff for HTTP calls (RegGovClient)
- in-memory checkpointing via ExecutionContext (works while JVM stays up)
- REST endpoint to trigger job on demand with validation

Build:
  mvn -DskipTests package

Run (CLI):
  java -jar target/regulations-batch-nodb-0.0.1-SNAPSHOT.jar --spring.batch.job.names=commentsPdfJob --apiKey=KEY --docketId=EPA-... --startCommentId=optional

Run (REST):
  POST /jobs/comments-pdf
  Body: {"apiKey":"...", "docketId":"...", "startCommentId":"..."}

Notes:
- No database is used. Job metadata and checkpointing are stored in-memory.
- For durable restart after JVM stop, a DB-backed JobRepository is required.
- Configure SharePoint credentials in application.yml or via env vars.

## Example REST Trigger Calls

Using curl:
```bash
curl -X POST http://localhost:8080/jobs/comments-pdf \
  -H "Content-Type: application/json" \
  -d '{"startCommentId":"YOUR_KEY","docketId":"EPA-HQ-OAR-2020-0001"}'
```

Using HTTPie:
```bash
http POST :8080/jobs/comments-pdf apiKey=YOUR_KEY docketId=EPA-HQ-OAR-2020-0001
```
