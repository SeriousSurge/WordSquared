#!/bin/bash

echo "🚀 Deploying Word Square Cloud Functions..."

# Create storage bucket if it doesn't exist
echo "📦 Creating storage bucket..."
gsutil mb gs://word-square-puzzles 2>/dev/null || echo "Bucket already exists"

# Deploy generator function
echo "🔧 Deploying generator function..."
cd generate-puzzles
gcloud functions deploy generate-puzzles \
  --runtime nodejs20 \
  --trigger-http \
  --allow-unauthenticated \
  --entry-point generatePuzzles \
  --memory 512MB \
  --timeout 540s

cd ..

# Deploy getter function  
echo "📥 Deploying getter function..."
cd get-puzzle
gcloud functions deploy get-puzzle \
  --runtime nodejs20 \
  --trigger-http \
  --allow-unauthenticated \
  --entry-point getPuzzle \
  --memory 256MB \
  --timeout 60s

cd ..

# Deploy words function
echo "📚 Deploying words function..."
cd get-words
gcloud functions deploy get-words \
  --runtime nodejs20 \
  --trigger-http \
  --allow-unauthenticated \
  --entry-point getWords \
  --memory 128MB \
  --timeout 60s

cd ..

echo "✅ Deployment complete!"
echo ""
echo "🔗 Your functions are now live:"
echo "Generator: https://us-central1-PROJECT_ID.cloudfunctions.net/generate-puzzles"
echo "Getter: https://us-central1-PROJECT_ID.cloudfunctions.net/get-puzzle"
echo "Words: https://us-central1-PROJECT_ID.cloudfunctions.net/get-words"
echo ""
echo "📅 To generate puzzles for 7 days:"
echo "curl 'https://us-central1-PROJECT_ID.cloudfunctions.net/generate-puzzles?days=7'"
echo ""
echo "🎯 To get today's puzzle:"
echo "curl 'https://us-central1-PROJECT_ID.cloudfunctions.net/get-puzzle'" 