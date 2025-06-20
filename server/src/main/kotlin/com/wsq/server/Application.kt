package com.wsq.server

import com.wsq.server.service.DailyWordSquareService
import com.wsq.server.service.WordSquareGenerator
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }
    
    val generator = WordSquareGenerator()
    val wordSquareService = DailyWordSquareService(generator)
    
    routing {
        // API endpoints for word squares
        get("/api/word-squares/today") {
            try {
                val wordSquares = wordSquareService.getTodaysWordSquares()
                call.respond(wordSquares)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error generating word squares: ${e.message}")
            }
        }
        
        get("/api/word-squares/{date}") {
            val date = call.parameters["date"]
            if (date == null) {
                call.respond(HttpStatusCode.BadRequest, "Date parameter is required")
                return@get
            }
            
            try {
                val wordSquares = wordSquareService.getWordSquaresForDate(date)
                call.respond(wordSquares)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error generating word squares: ${e.message}")
            }
        }
        
        get("/api/word-squares/{date}/{difficulty}") {
            val date = call.parameters["date"]
            val difficulty = call.parameters["difficulty"]
            
            if (date == null || difficulty == null) {
                call.respond(HttpStatusCode.BadRequest, "Date and difficulty parameters are required")
                return@get
            }
            
            try {
                val wordSquares = wordSquareService.getWordSquaresForDate(date)
                val specificPuzzle = wordSquares.puzzles[difficulty]
                
                if (specificPuzzle != null) {
                    call.respond(specificPuzzle)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Word square not found for difficulty: $difficulty")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error generating word square: ${e.message}")
            }
        }
        
        get("/api/dates") {
            try {
                val dates = wordSquareService.getAvailableDates()
                call.respond(mapOf("dates" to dates))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error getting dates: ${e.message}")
            }
        }
        
        // Root path - serve the main HTML page
        get("/") {
            call.respondText(
                contentType = ContentType.Text.Html,
                text = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Daily Word Square Generator</title>
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            max-width: 1200px;
                            margin: 0 auto;
                            padding: 20px;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            min-height: 100vh;
                            color: white;
                        }
                        .container {
                            background: rgba(255, 255, 255, 0.1);
                            backdrop-filter: blur(10px);
                            border-radius: 20px;
                            padding: 30px;
                            box-shadow: 0 8px 32px rgba(31, 38, 135, 0.37);
                        }
                        h1 {
                            text-align: center;
                            margin-bottom: 30px;
                            font-size: 2.5em;
                            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
                        }
                        .controls {
                            display: flex;
                            gap: 20px;
                            margin-bottom: 30px;
                            justify-content: center;
                            flex-wrap: wrap;
                        }
                        button, select {
                            padding: 12px 24px;
                            border: none;
                            border-radius: 8px;
                            font-size: 16px;
                            cursor: pointer;
                            transition: all 0.3s ease;
                            background: rgba(255, 255, 255, 0.2);
                            color: white;
                            border: 1px solid rgba(255, 255, 255, 0.3);
                        }
                        button:hover, select:hover {
                            background: rgba(255, 255, 255, 0.3);
                            transform: translateY(-2px);
                        }
                        .puzzle-info {
                            background: rgba(255, 255, 255, 0.1);
                            padding: 20px;
                            border-radius: 10px;
                            margin: 20px 0;
                        }
                        .word-square-grid {
                            display: grid;
                            gap: 2px;
                            background: #333;
                            border: 2px solid #333;
                            margin: 20px 0;
                            justify-content: center;
                            max-width: 300px;
                            margin: 20px auto;
                        }
                        .cell {
                            width: 40px;
                            height: 40px;
                            background: white;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-weight: bold;
                            color: black;
                            font-size: 18px;
                        }
                        .cell.editable {
                            background: #e8f4fd;
                            border: 2px solid #2196F3;
                        }
                        .target-words {
                            display: grid;
                            grid-template-columns: 1fr 1fr;
                            gap: 20px;
                            margin-top: 20px;
                        }
                        .word-target {
                            background: rgba(255, 255, 255, 0.1);
                            padding: 15px;
                            border-radius: 8px;
                            text-align: center;
                        }
                        .loading {
                            text-align: center;
                            font-size: 18px;
                            margin: 40px 0;
                        }
                        .error {
                            color: #ff6b6b;
                            text-align: center;
                            padding: 20px;
                            background: rgba(255, 107, 107, 0.1);
                            border-radius: 10px;
                            margin: 20px 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>🧩 Daily Word Square Generator</h1>
                        
                        <div class="controls">
                            <input type="date" id="dateInput" />
                            <select id="difficultySelect">
                                <option value="4x4">4x4 Grid</option>
                                <option value="5x5">5x5 Grid</option>
                                <option value="6x6">6x6 Grid</option>
                            </select>
                            <button onclick="loadTodaysWordSquare()">Today's Puzzle</button>
                            <button onclick="loadWordSquare()">Load Puzzle</button>
                        </div>
                        
                        <div id="content">
                            <div class="loading">Click "Today's Puzzle" to get started!</div>
                        </div>
                    </div>

                    <script>
                        // Set today's date as default
                        document.getElementById('dateInput').valueAsDate = new Date();
                        
                        async function loadTodaysWordSquare() {
                            const difficulty = document.getElementById('difficultySelect').value;
                            document.getElementById('content').innerHTML = '<div class="loading">Loading today\'s word square...</div>';
                            
                            try {
                                const response = await fetch('/api/word-squares/today');
                                const data = await response.json();
                                displayWordSquare(data.puzzles[difficulty]);
                            } catch (error) {
                                displayError('Failed to load today\'s word square: ' + error.message);
                            }
                        }
                        
                        async function loadWordSquare() {
                            const date = document.getElementById('dateInput').value;
                            const difficulty = document.getElementById('difficultySelect').value;
                            
                            if (!date) {
                                displayError('Please select a date');
                                return;
                            }
                            
                            document.getElementById('content').innerHTML = '<div class="loading">Loading word square...</div>';
                            
                            try {
                                const response = await fetch(`/api/word-squares/${'$'}{date}/${'$'}{difficulty}`);
                                const puzzle = await response.json();
                                displayWordSquare(puzzle);
                            } catch (error) {
                                displayError('Failed to load word square: ' + error.message);
                            }
                        }
                        
                        function displayWordSquare(puzzle) {
                            if (!puzzle) {
                                displayError('No word square data received');
                                return;
                            }
                            
                            const content = document.getElementById('content');
                            content.innerHTML = `
                                <div class="puzzle-info">
                                    <h2>Word Square for ${'$'}{puzzle.date}</h2>
                                    <p><strong>Difficulty:</strong> ${'$'}{puzzle.difficulty}</p>
                                    <p><strong>Size:</strong> ${'$'}{puzzle.size} x ${'$'}{puzzle.size}</p>
                                    <p><strong>Instructions:</strong> Fill the border cells to create valid words along the edges</p>
                                </div>
                                
                                <div class="word-square-grid" style="grid-template-columns: repeat(${'$'}{puzzle.size}, 40px);">
                                    ${'$'}{generateWordSquareGrid(puzzle)}
                                </div>
                                
                                <div class="target-words">
                                    <div class="word-target">
                                        <h3>Target Words</h3>
                                        <p><strong>Top:</strong> ${'$'}{puzzle.targetWords.topWord}</p>
                                        <p><strong>Bottom:</strong> ${'$'}{puzzle.targetWords.bottomWord}</p>
                                        <p><strong>Left:</strong> ${'$'}{puzzle.targetWords.leftWord}</p>
                                        <p><strong>Right:</strong> ${'$'}{puzzle.targetWords.rightWord}</p>
                                    </div>
                                    <div class="word-target">
                                        <h3>Solution Grid</h3>
                                        <div style="font-family: monospace; line-height: 1.2;">
                                            ${'$'}{puzzle.solution.map(row => row.join(' ')).join('<br>')}
                                        </div>
                                    </div>
                                </div>
                            `;
                        }
                        
                        function generateWordSquareGrid(puzzle) {
                            let html = '';
                            for (let row = 0; row < puzzle.size; row++) {
                                for (let col = 0; col < puzzle.size; col++) {
                                    const isEditable = puzzle.editableCells.some(([r, c]) => r === row && c === col);
                                    const letter = puzzle.solution[row][col];
                                    const cellClass = isEditable ? 'cell editable' : 'cell';
                                    html += `<div class="${'$'}{cellClass}">${'$'}{letter !== ' ' ? letter : ''}</div>`;
                                }
                            }
                            return html;
                        }
                        
                        function displayError(message) {
                            document.getElementById('content').innerHTML = `<div class="error">${'$'}{message}</div>`;
                        }
                    </script>
                </body>
                </html>
                """.trimIndent()
            )
        }
    }
} 