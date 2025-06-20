// Vercel function to generate word square puzzles
const WORD_LISTS = {
  easy: ['LOVE', 'CARE', 'HOPE', 'NICE', 'GOOD', 'FIND', 'MAKE', 'TAKE', 'COME', 'KNOW'],
  medium: ['PRIME', 'BLEND', 'CRAFT', 'DREAM', 'GRACE', 'HEART', 'LIGHT', 'MAGIC', 'PEACE', 'SPARK'],
  hard: ['SQUIRE', 'PRINCE', 'MASTER', 'SOLVER', 'BRIDGE', 'CHARGE', 'FLIGHT', 'RHYTHM', 'SMOOTH', 'BRONZE']
};

function generateWordSquare(size) {
  const wordList = WORD_LISTS[size === 4 ? 'easy' : size === 5 ? 'medium' : 'hard'];
  
  // Pick random words
  const words = [];
  for (let i = 0; i < 4; i++) {
    words.push(wordList[Math.floor(Math.random() * wordList.length)]);
  }
  
  // Create the grid with intersecting pattern
  const [topWord, rightWord, bottomWord, leftWord] = words;
  
  // Create grid
  const grid = Array(size).fill().map(() => Array(size).fill(''));
  
  // Place words
  // Top edge
  for (let i = 0; i < size; i++) {
    grid[0][i] = topWord[i];
  }
  
  // Bottom edge  
  for (let i = 0; i < size; i++) {
    grid[size-1][i] = bottomWord[i];
  }
  
  // Left edge
  for (let i = 0; i < size; i++) {
    grid[i][0] = leftWord[i];
  }
  
  // Right edge
  for (let i = 0; i < size; i++) {
    grid[i][size-1] = rightWord[i];
  }
  
  // Fill interior with random letters
  for (let i = 1; i < size-1; i++) {
    for (let j = 1; j < size-1; j++) {
      grid[i][j] = String.fromCharCode(65 + Math.floor(Math.random() * 26));
    }
  }
  
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
}

export default function handler(req, res) {
  try {
    const { days = 7 } = req.query;
    const startDate = new Date();
    const puzzles = {};
    
    for (let i = 0; i < parseInt(days); i++) {
      const date = new Date(startDate);
      date.setDate(startDate.getDate() + i);
      const dateKey = date.toISOString().split('T')[0];
      
      // Generate puzzles for each size
      const dailyPuzzles = {
        date: dateKey,
        puzzles: {
          easy: generateWordSquare(4),
          medium: generateWordSquare(5), 
          hard: generateWordSquare(6)
        }
      };
      
      puzzles[dateKey] = dailyPuzzles;
    }
    
    res.json({
      success: true,
      message: `Generated puzzles for ${Object.keys(puzzles).length} days`,
      puzzles,
      generatedAt: new Date().toISOString()
    });
    
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
} 