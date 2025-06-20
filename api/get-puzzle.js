// Simple in-memory storage for demo (in production, use a database)
let puzzleStorage = {};

export default function handler(req, res) {
  try {
    const { date, difficulty } = req.query;
    const targetDate = date || new Date().toISOString().split('T')[0];
    
    // If no puzzles in memory, generate them on-demand
    if (!puzzleStorage[targetDate]) {
      // Generate puzzle for this date (using date as seed for consistency)
      const seedDate = new Date(targetDate);
      const seed = seedDate.getTime();
      
      // Simple seeded random function
      function seededRandom(seed) {
        const x = Math.sin(seed) * 10000;
        return x - Math.floor(x);
      }
      
      const WORD_LISTS = {
        easy: ['LOVE', 'CARE', 'HOPE', 'NICE', 'GOOD', 'FIND', 'MAKE', 'TAKE', 'COME', 'KNOW'],
        medium: ['PRIME', 'BLEND', 'CRAFT', 'DREAM', 'GRACE', 'HEART', 'LIGHT', 'MAGIC', 'PEACE', 'SPARK'],
        hard: ['SQUIRE', 'PRINCE', 'MASTER', 'SOLVER', 'BRIDGE', 'CHARGE', 'FLIGHT', 'RHYTHM', 'SMOOTH', 'BRONZE']
      };
      
      function generateSeededWordSquare(size, dateSeed) {
        const wordList = WORD_LISTS[size === 4 ? 'easy' : size === 5 ? 'medium' : 'hard'];
        
        // Pick seeded random words for consistent daily puzzles
        const words = [];
        for (let i = 0; i < 4; i++) {
          const index = Math.floor(seededRandom(dateSeed + i * 1000) * wordList.length);
          words.push(wordList[index]);
        }
        
        const [topWord, rightWord, bottomWord, leftWord] = words;
        const grid = Array(size).fill().map(() => Array(size).fill(''));
        
        // Place words
        for (let i = 0; i < size; i++) {
          grid[0][i] = topWord[i];
          grid[size-1][i] = bottomWord[i];
          grid[i][0] = leftWord[i];
          grid[i][size-1] = rightWord[i];
        }
        
        // Fill interior with seeded random letters
        for (let i = 1; i < size-1; i++) {
          for (let j = 1; j < size-1; j++) {
            const letterIndex = Math.floor(seededRandom(dateSeed + i * 100 + j * 10) * 26);
            grid[i][j] = String.fromCharCode(65 + letterIndex);
          }
        }
        
        return {
          grid,
          targets: { top: topWord, right: rightWord, bottom: bottomWord, left: leftWord },
          size
        };
      }
      
      // Generate and cache the puzzle
      puzzleStorage[targetDate] = {
        date: targetDate,
        puzzles: {
          easy: generateSeededWordSquare(4, seed),
          medium: generateSeededWordSquare(5, seed + 10000),
          hard: generateSeededWordSquare(6, seed + 20000)
        }
      };
    }
    
    const puzzleData = puzzleStorage[targetDate];
    
    // Return specific difficulty or all
    if (difficulty && puzzleData.puzzles[difficulty]) {
      res.json({
        date: targetDate,
        difficulty,
        puzzle: puzzleData.puzzles[difficulty]
      });
    } else {
      res.json(puzzleData);
    }
    
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
} 