const axios = require('axios');
const { Storage } = require('@google-cloud/storage');
const wordsData = require('./words.json');

const storage = new Storage();
const BUCKET_NAME = 'word-square-puzzles';

// Use words from the JSON file
const WORD_LISTS = {
  easy: wordsData['4_letter_words'],
  medium: wordsData['5_letter_words'],
  hard: wordsData['6_letter_words']
};

async function isValidWord(word) {
  try {
    const response = await axios.get(`https://api.datamuse.com/words?sp=${word}&max=1`);
    return response.data.length > 0;
  } catch (error) {
    console.error('Error checking word:', error);
    return false;
  }
}

async function generateWordSquare(size) {
  const wordList = WORD_LISTS[size === 4 ? 'easy' : size === 5 ? 'medium' : 'hard'];
  const maxAttempts = 1000;
  
  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    try {
      // Find a valid set of 4 words that can form proper intersections
      const topWord = wordList[Math.floor(Math.random() * wordList.length)].toUpperCase();
      
      // Find words that can intersect with the top word
      const possibleLeftWords = wordList.filter(word => 
        word.toUpperCase()[0] === topWord[0] && word.toUpperCase().length === size
      );
      
      if (possibleLeftWords.length === 0) continue;
      
      const leftWord = possibleLeftWords[Math.floor(Math.random() * possibleLeftWords.length)].toUpperCase();
      
      // Find right word that starts with last letter of top word
      const possibleRightWords = wordList.filter(word => 
        word.toUpperCase()[0] === topWord[size - 1] && word.toUpperCase().length === size
      );
      
      if (possibleRightWords.length === 0) continue;
      
      const rightWord = possibleRightWords[Math.floor(Math.random() * possibleRightWords.length)].toUpperCase();
      
      // Find bottom word that starts with last letter of left word and ends with last letter of right word
      const possibleBottomWords = wordList.filter(word => {
        const upperWord = word.toUpperCase();
        return upperWord[0] === leftWord[size - 1] && 
               upperWord[size - 1] === rightWord[size - 1] && 
               upperWord.length === size;
      });
      
      if (possibleBottomWords.length === 0) continue;
      
      const bottomWord = possibleBottomWords[Math.floor(Math.random() * possibleBottomWords.length)].toUpperCase();
      
      // Verify all corner constraints
      if (topWord[0] !== leftWord[0] ||          // Top-left corner
          topWord[size-1] !== rightWord[0] ||    // Top-right corner
          leftWord[size-1] !== bottomWord[0] ||  // Bottom-left corner
          rightWord[size-1] !== bottomWord[size-1]) { // Bottom-right corner
        continue;
      }
      
      // Create grid with proper intersections
      const grid = Array(size).fill().map(() => Array(size).fill(''));
      
      // Place the border words
      for (let i = 0; i < size; i++) {
        grid[0][i] = topWord[i];        // Top edge
        grid[size-1][i] = bottomWord[i]; // Bottom edge
        grid[i][0] = leftWord[i];       // Left edge
        grid[i][size-1] = rightWord[i]; // Right edge
      }
      
      // Fill interior with random letters (these don't matter for the game)
      for (let i = 1; i < size-1; i++) {
        for (let j = 1; j < size-1; j++) {
          grid[i][j] = String.fromCharCode(65 + Math.floor(Math.random() * 26));
        }
      }
      
      // Validate that we have a proper word square
      console.log(`Generated valid word square:
        Top: ${topWord} (${topWord[0]}...${topWord[size-1]})
        Left: ${leftWord} (${leftWord[0]}...${leftWord[size-1]})
        Right: ${rightWord} (${rightWord[0]}...${rightWord[size-1]})
        Bottom: ${bottomWord} (${bottomWord[0]}...${bottomWord[size-1]})
        Corners: TL=${topWord[0]}, TR=${topWord[size-1]}, BL=${leftWord[size-1]}, BR=${rightWord[size-1]}`);
      
      return {
        grid,
        targets: {
          top: topWord,
          right: rightWord,
          bottom: bottomWord,
          left: leftWord
        },
        size
      };
      
    } catch (error) {
      console.error('Attempt failed:', error);
      continue;
    }
  }
  
  throw new Error(`Failed to generate ${size}x${size} word square after ${maxAttempts} attempts`);
}

exports.generatePuzzles = async (req, res) => {
  try {
    const { days = 7 } = req.query;
    const startDate = new Date();
    const puzzles = {};
    
    console.log(`Generating puzzles for ${days} days starting from ${startDate.toISOString()}`);
    console.log(`Using ${WORD_LISTS.easy.length} 4-letter words, ${WORD_LISTS.medium.length} 5-letter words, ${WORD_LISTS.hard.length} 6-letter words`);
    
    for (let i = 0; i < parseInt(days); i++) {
      const date = new Date(startDate);
      date.setDate(startDate.getDate() + i);
      const dateKey = date.toISOString().split('T')[0];
      
      console.log(`Generating puzzles for ${dateKey}`);
      
      // Generate puzzles for each size
      const dailyPuzzles = {
        date: dateKey,
        puzzles: {
          easy: await generateWordSquare(4),
          medium: await generateWordSquare(5), 
          hard: await generateWordSquare(6)
        }
      };
      
      puzzles[dateKey] = dailyPuzzles;
      
      // Store in Cloud Storage
      const file = storage.bucket(BUCKET_NAME).file(`puzzles/${dateKey}.json`);
      await file.save(JSON.stringify(dailyPuzzles, null, 2), {
        metadata: {
          contentType: 'application/json',
        },
      });
    }
    
    console.log(`Successfully generated ${Object.keys(puzzles).length} days of puzzles`);
    
    res.json({
      success: true,
      message: `Generated puzzles for ${Object.keys(puzzles).length} days`,
      dates: Object.keys(puzzles),
      generatedAt: new Date().toISOString(),
      wordCounts: {
        easy: WORD_LISTS.easy.length,
        medium: WORD_LISTS.medium.length,
        hard: WORD_LISTS.hard.length
      }
    });
    
  } catch (error) {
    console.error('Error generating puzzles:', error);
    res.status(500).json({
      success: false,
      error: error.message,
      stack: error.stack
    });
  }
}; 