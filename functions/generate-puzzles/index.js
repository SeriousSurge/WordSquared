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

/**
 * Validates that a word square has proper intersections
 */
function validateWordSquareIntersections(topWord, leftWord, rightWord, bottomWord, size) {
  // Check corner intersections
  if (!topWord || !leftWord || !rightWord || !bottomWord) return false;
  
  // All words must be exactly the right size
  if (topWord.length !== size || leftWord.length !== size || 
      rightWord.length !== size || bottomWord.length !== size) return false;
  
  // Corner intersections must match exactly
  const topLeft = topWord[0] === leftWord[0];
  const topRight = topWord[size - 1] === rightWord[0];
  const bottomLeft = leftWord[size - 1] === bottomWord[0];
  const bottomRight = rightWord[size - 1] === bottomWord[size - 1];
  
  if (!topLeft || !topRight || !bottomLeft || !bottomRight) {
    console.log(`Corner validation failed:
      TopLeft: ${topWord[0]} === ${leftWord[0]} : ${topLeft}
      TopRight: ${topWord[size - 1]} === ${rightWord[0]} : ${topRight}
      BottomLeft: ${leftWord[size - 1]} === ${bottomWord[0]} : ${bottomLeft}
      BottomRight: ${rightWord[size - 1]} === ${bottomWord[size - 1]} : ${bottomRight}`);
    return false;
  }
  
  return true;
}

/**
 * Creates a proper word square with validated intersections
 */
function createValidatedWordSquareGrid(topWord, leftWord, rightWord, bottomWord, size) {
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
  
  return grid;
}

async function generateWordSquare(size) {
  const wordList = WORD_LISTS[size === 4 ? 'easy' : size === 5 ? 'medium' : 'hard'];
  const maxAttempts = 2000; // Increased attempts for better success rate
  
  console.log(`Generating ${size}x${size} word square from ${wordList.length} words...`);
  
  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    try {
      // Step 1: Pick a random starting word for the top
      const topWord = wordList[Math.floor(Math.random() * wordList.length)].toUpperCase();
      
      // Step 2: Find words that can intersect with the top word at position 0
      const possibleLeftWords = wordList.filter(word => 
        word.toUpperCase()[0] === topWord[0] && 
        word.toUpperCase().length === size &&
        word.toUpperCase() !== topWord // Avoid duplicates
      );
      
      if (possibleLeftWords.length === 0) {
        if (attempt % 100 === 0) console.log(`Attempt ${attempt}: No left words for top word "${topWord}"`);
        continue;
      }
      
      const leftWord = possibleLeftWords[Math.floor(Math.random() * possibleLeftWords.length)].toUpperCase();
      
      // Step 3: Find words that can intersect with the top word at the last position
      const possibleRightWords = wordList.filter(word => 
        word.toUpperCase()[0] === topWord[size - 1] && 
        word.toUpperCase().length === size &&
        word.toUpperCase() !== topWord && // Avoid duplicates
        word.toUpperCase() !== leftWord
      );
      
      if (possibleRightWords.length === 0) {
        if (attempt % 100 === 0) console.log(`Attempt ${attempt}: No right words for top word "${topWord}"`);
        continue;
      }
      
      const rightWord = possibleRightWords[Math.floor(Math.random() * possibleRightWords.length)].toUpperCase();
      
      // Step 4: Find words that connect the bottom of left word to bottom of right word
      const possibleBottomWords = wordList.filter(word => {
        const upperWord = word.toUpperCase();
        return upperWord[0] === leftWord[size - 1] && 
               upperWord[size - 1] === rightWord[size - 1] && 
               upperWord.length === size &&
               upperWord !== topWord && // Avoid duplicates
               upperWord !== leftWord &&
               upperWord !== rightWord;
      });
      
      if (possibleBottomWords.length === 0) {
        if (attempt % 100 === 0) console.log(`Attempt ${attempt}: No bottom words connecting "${leftWord[size-1]}" to "${rightWord[size-1]}"`);
        continue;
      }
      
      const bottomWord = possibleBottomWords[Math.floor(Math.random() * possibleBottomWords.length)].toUpperCase();
      
      // Step 5: Validate the complete word square
      if (!validateWordSquareIntersections(topWord, leftWord, rightWord, bottomWord, size)) {
        console.log(`Attempt ${attempt}: Validation failed for words: ${topWord}, ${leftWord}, ${rightWord}, ${bottomWord}`);
        continue;
      }
      
      // Step 6: Create the validated grid
      const grid = createValidatedWordSquareGrid(topWord, leftWord, rightWord, bottomWord, size);
      
      // Final validation log
      console.log(`✅ Generated valid ${size}x${size} word square (attempt ${attempt + 1}):
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
      console.error(`Attempt ${attempt} failed:`, error.stack);
      continue;
    }
  }
  
  throw new Error(`❌ Failed to generate valid ${size}x${size} word square after ${maxAttempts} attempts`);
}

exports.generatePuzzles = async (req, res) => {
  try {
    const { days = 7 } = req.query;
    const daysInt = parseInt(days, 10);
    if (!Number.isInteger(daysInt) || daysInt < 1 || daysInt > 30) {
      res.status(400).json({
        success: false,
        error: 'Invalid "days" parameter. Must be an integer between 1 and 30.'
      });
      return;
    }
    const startDate = new Date();
    const puzzles = {};

    console.log(`Generating puzzles for ${daysInt} days starting from ${startDate.toISOString()}`);
    console.log(`Using ${WORD_LISTS.easy.length} 4-letter words, ${WORD_LISTS.medium.length} 5-letter words, ${WORD_LISTS.hard.length} 6-letter words`);
    
    for (let i = 0; i < daysInt; i++) {
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
    console.error('Error generating puzzles:', error.stack);
    res.status(500).json({
      success: false,
      error: 'Internal server error'
    });
  }
};