const { Storage } = require('@google-cloud/storage');
const wordsData = require('./words.json');

const storage = new Storage();
const BUCKET_NAME = 'word-square-puzzles';

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
  
  return topLeft && topRight && bottomLeft && bottomRight;
}

exports.getPuzzle = async (req, res) => {
  // Set CORS headers for all responses
  res.set('Access-Control-Allow-Origin', '*');

  if (req.method === 'OPTIONS') {
    // Send response to OPTIONS requests
    res.set('Access-Control-Allow-Methods', 'GET');
    res.set('Access-Control-Allow-Headers', 'Content-Type');
    res.set('Access-Control-Max-Age', '3600');
    res.status(204).send('');
    return;
  }
  
  try {
    // Get date from query params or use today
    const { date, difficulty } = req.query;
    if (date && !/^\d{4}-\d{2}-\d{2}$/.test(date)) {
      res.status(400).json({
        success: false,
        error: 'Invalid "date" format. Expected YYYY-MM-DD.'
      });
      return;
    }
    const targetDate = date || new Date().toISOString().split('T')[0];
    
    console.log(`Fetching puzzle for date: ${targetDate}, difficulty: ${difficulty || 'all'}`);
    
    // Try to get from Cloud Storage
    const file = storage.bucket(BUCKET_NAME).file(`puzzles/${targetDate}.json`);
    
    try {
      const [contents] = await file.download();
      const puzzleData = JSON.parse(contents.toString());
      
      // If specific difficulty requested, return just that
      if (difficulty && puzzleData.puzzles[difficulty]) {
        res.json({
          date: targetDate,
          difficulty,
          puzzle: puzzleData.puzzles[difficulty]
        });
      } else {
        // Return all difficulties
        res.json(puzzleData);
      }
      
    } catch (downloadError) {
      if (downloadError.code === 404) {
        // Generate puzzle on-demand using the same logic as the generator
        console.log(`No stored puzzle for ${targetDate}, generating on-demand...`);
        
        const seedDate = new Date(targetDate);
        const seed = seedDate.getTime();
        
        // Use words from the JSON file
        const WORD_LISTS = {
          easy: wordsData['4_letter_words'],
          medium: wordsData['5_letter_words'],
          hard: wordsData['6_letter_words']
        };
        
        // Simple seeded random function
        function seededRandom(seed) {
          const x = Math.sin(seed) * 10000;
          return x - Math.floor(x);
        }
        
        function generateSeededWordSquare(size, dateSeed) {
          const wordList = WORD_LISTS[size === 4 ? 'easy' : size === 5 ? 'medium' : 'hard'];
          const maxAttempts = 2000; // Increased for better success rate
          
          console.log(`Generating seeded ${size}x${size} word square from ${wordList.length} words...`);
          
          for (let attempt = 0; attempt < maxAttempts; attempt++) {
            try {
              // Step 1: Pick a seeded starting word for the top
              const topIndex = Math.floor(seededRandom(dateSeed + attempt) * wordList.length);
              const topWord = wordList[topIndex].toUpperCase();
              
              // Step 2: Find words that can intersect with the top word at position 0
              const possibleLeftWords = wordList.filter(word => 
                word.toUpperCase()[0] === topWord[0] && 
                word.toUpperCase().length === size &&
                word.toUpperCase() !== topWord
              );
              
              if (possibleLeftWords.length === 0) continue;
              
              const leftIndex = Math.floor(seededRandom(dateSeed + attempt + 1000) * possibleLeftWords.length);
              const leftWord = possibleLeftWords[leftIndex].toUpperCase();
              
              // Step 3: Find words that can intersect with the top word at the last position
              const possibleRightWords = wordList.filter(word => 
                word.toUpperCase()[0] === topWord[size - 1] && 
                word.toUpperCase().length === size &&
                word.toUpperCase() !== topWord &&
                word.toUpperCase() !== leftWord
              );
              
              if (possibleRightWords.length === 0) continue;
              
              const rightIndex = Math.floor(seededRandom(dateSeed + attempt + 2000) * possibleRightWords.length);
              const rightWord = possibleRightWords[rightIndex].toUpperCase();
              
              // Step 4: Find words that connect the bottom of left word to bottom of right word
              const possibleBottomWords = wordList.filter(word => {
                const upperWord = word.toUpperCase();
                return upperWord[0] === leftWord[size - 1] && 
                       upperWord[size - 1] === rightWord[size - 1] && 
                       upperWord.length === size &&
                       upperWord !== topWord &&
                       upperWord !== leftWord &&
                       upperWord !== rightWord;
              });
              
              if (possibleBottomWords.length === 0) continue;
              
              const bottomIndex = Math.floor(seededRandom(dateSeed + attempt + 3000) * possibleBottomWords.length);
              const bottomWord = possibleBottomWords[bottomIndex].toUpperCase();
              
              // Step 5: Validate the complete word square
              if (!validateWordSquareIntersections(topWord, leftWord, rightWord, bottomWord, size)) {
                continue;
              }
              
              // Step 6: Create the validated grid
              const grid = Array(size).fill().map(() => Array(size).fill(''));
              
              // Place the border words
              for (let i = 0; i < size; i++) {
                grid[0][i] = topWord[i];        // Top edge
                grid[size-1][i] = bottomWord[i]; // Bottom edge
                grid[i][0] = leftWord[i];       // Left edge
                grid[i][size-1] = rightWord[i]; // Right edge
              }
              
              // Fill interior with seeded random letters
              for (let i = 1; i < size-1; i++) {
                for (let j = 1; j < size-1; j++) {
                  const letterIndex = Math.floor(seededRandom(dateSeed + i * 100 + j * 10) * 26);
                  grid[i][j] = String.fromCharCode(65 + letterIndex);
                }
              }
              
              console.log(`✅ Generated seeded ${size}x${size} word square for ${targetDate} (attempt ${attempt + 1}):
                Top: ${topWord} (${topWord[0]}...${topWord[size-1]})
                Left: ${leftWord} (${leftWord[0]}...${leftWord[size-1]})
                Right: ${rightWord} (${rightWord[0]}...${rightWord[size-1]})
                Bottom: ${bottomWord} (${bottomWord[0]}...${bottomWord[size-1]})
                Corners: TL=${topWord[0]}, TR=${topWord[size-1]}, BL=${leftWord[size-1]}, BR=${rightWord[size-1]}`);
              
              return {
                grid,
                targets: { top: topWord, right: rightWord, bottom: bottomWord, left: leftWord },
                size
              };
              
            } catch (error) {
              console.error('Seeded generation attempt failed:', error.stack);
              continue;
            }
          }
          
          throw new Error(`❌ Failed to generate seeded ${size}x${size} word square after ${maxAttempts} attempts`);
        }
        
        // Generate and return the puzzle
        const puzzleData = {
          date: targetDate,
          puzzles: {
            easy: generateSeededWordSquare(4, seed),
            medium: generateSeededWordSquare(5, seed + 10000),
            hard: generateSeededWordSquare(6, seed + 20000)
          }
        };
        
        // Return specific difficulty or all
        if (difficulty && puzzleData.puzzles[difficulty]) {
          res.json({
            date: targetDate,
            difficulty,
            puzzle: puzzleData.puzzles[difficulty],
            generated: 'on-demand'
          });
        } else {
          res.json({
            ...puzzleData,
            generated: 'on-demand'
          });
        }
      } else {
        console.error('Error downloading puzzle:', downloadError.stack);
        throw downloadError;
      }
    }
    
  } catch (error) {
    console.error('Error fetching puzzle:', error.stack);
    // Ensure CORS header is set on error responses too
    res.set('Access-Control-Allow-Origin', '*');
    res.status(500).json({
      success: false,
      error: 'Internal server error'
    });
  }
};

// Health check endpoint
exports.health = async (req, res) => {
  res.json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    service: 'get-puzzle',
    wordCounts: {
      easy: wordsData['4_letter_words'].length,
      medium: wordsData['5_letter_words'].length,
      hard: wordsData['6_letter_words'].length
    }
  });
}; 