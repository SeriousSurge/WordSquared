# Word Square Cloud Functions

**Much more economical than a full server!** ⚡️

This serverless approach uses two simple cloud functions:
1. **Generator** - Creates puzzles for multiple days and stores them
2. **Getter** - Retrieves puzzles by date

## 💰 Cost Comparison

| Solution | Cost |
|----------|------|
| **Always-on server** | $5-20/month |
| **Cloud Functions** | ~$0.10/month |

## 🚀 Quick Deploy

```bash
# Make deploy script executable
chmod +x deploy.sh

# Deploy both functions
./deploy.sh
```

## 📡 API Endpoints

Once deployed, you'll have:

### Generate Puzzles
```
GET https://us-central1-PROJECT_ID.cloudfunctions.net/generate-puzzles?days=7
```

**Response:**
```json
{
  "success": true,
  "message": "Generated puzzles for 7 days",
  "dates": ["2024-06-13", "2024-06-14", "..."],
  "generatedAt": "2024-06-13T20:00:00.000Z"
}
```

### Get Puzzle
```
GET https://us-central1-PROJECT_ID.cloudfunctions.net/get-puzzle
GET https://us-central1-PROJECT_ID.cloudfunctions.net/get-puzzle?date=2024-06-13
GET https://us-central1-PROJECT_ID.cloudfunctions.net/get-puzzle?date=2024-06-13&difficulty=easy
```

**Response:**
```json
{
  "date": "2024-06-13",
  "puzzles": {
    "easy": {
      "grid": [["L","O","V","E"], ["O","X","X","N"], ["V","X","X","D"], ["E","N","D","S"]],
      "targets": {"top": "LOVE", "right": "ENDS", "bottom": "ENDS", "left": "LOVE"},
      "size": 4
    },
    "medium": { /* 5x5 puzzle */ },
    "hard": { /* 6x6 puzzle */ }
  }
}
```

## 🔄 Automation Options

### Option 1: Cloud Scheduler (Recommended)
Set up a weekly cron job to generate puzzles:

```bash
gcloud scheduler jobs create http generate-weekly-puzzles \
  --schedule="0 0 * * 0" \
  --uri="https://us-central1-PROJECT_ID.cloudfunctions.net/generate-puzzles?days=7" \
  --http-method=GET
```

### Option 2: Manual Generation
Generate puzzles as needed:

```bash
# Generate 14 days of puzzles
curl 'https://your-function-url/generate-puzzles?days=14'
```

## 📱 Mobile App Integration

Update your app to use the getter function:

```kotlin
// In your mobile app
const val PUZZLE_API = "https://us-central1-PROJECT_ID.cloudfunctions.net/get-puzzle"

// Get today's puzzle
val response = httpClient.get(PUZZLE_API)

// Get specific date
val response = httpClient.get("$PUZZLE_API?date=2024-06-13")

// Get specific difficulty
val response = httpClient.get("$PUZZLE_API?date=2024-06-13&difficulty=easy")
```

## 🏗️ Architecture

```
📱 Mobile App
    ↓
🔗 Cloud Function (get-puzzle)
    ↓
☁️ Cloud Storage (puzzles/*.json)
    ↑
⚙️ Cloud Function (generate-puzzles)
    ↑
⏰ Cloud Scheduler (weekly)
```

## 🎯 Benefits

- **💰 Ultra-cheap**: Only pay when functions run
- **🔄 Scalable**: Handles any traffic automatically  
- **🛠️ Simple**: No server maintenance
- **⚡️ Fast**: Functions start in milliseconds
- **🔒 Secure**: Google handles all security

## 🚀 Alternative Platforms

The same functions work on:
- **Vercel Functions** (even easier deploy)
- **AWS Lambda** (most powerful)
- **Netlify Functions** (great for static sites)

Want me to convert to any of these platforms? 