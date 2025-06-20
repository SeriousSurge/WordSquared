package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VirtualKeyboard(
    onKeyPress: (Char) -> Unit,
    onEnter: () -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Keyboard rows
        val rows = listOf(
            "QWERTYUIOP",
            "ASDFGHJKL",
            "ZXCVBNM"
        )
        
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { letter ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(1.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                            .clickable { onKeyPress(letter) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // Bottom row with backspace and submit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .padding(1.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                    .clickable { onBackspace() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Box(
                modifier = Modifier
                    .weight(2f)
                    .height(40.dp)
                    .padding(1.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                    .clickable { onEnter() },
                contentAlignment = Alignment.Center
            ) {
                Text("Submit", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun SplitKeyboardLeft(
    onKeyPress: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Left side letters: Q W E R T, A S D F, Z X C
        val leftRows = listOf(
            "QWERT",
            "ASDF",
            "ZXC"
        )
        
        leftRows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                row.forEach { letter ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                            .clickable { onKeyPress(letter) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // Backspace button
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(36.dp)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                .clickable { onBackspace() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun SplitKeyboardRight(
    onKeyPress: (Char) -> Unit,
    onEnter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Right side letters: Y U I O P, G H J K L, V B N M
        val rightRows = listOf(
            "YUIOP",
            "GHJKL",
            "VBNM"
        )
        
        rightRows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                row.forEach { letter ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                            .clickable { onKeyPress(letter) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // Submit button
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(36.dp)
                .background(Color(0xFF4169E1), RoundedCornerShape(4.dp))
                .clickable { onEnter() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Submit",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
} 