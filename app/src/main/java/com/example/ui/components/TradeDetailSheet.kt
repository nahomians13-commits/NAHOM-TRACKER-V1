package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.PositionType
import com.example.data.Trade
import com.example.data.TradeResult
import com.example.ui.theme.BreakEvenAmber
import com.example.ui.theme.BreakEvenDark
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonLoss
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeDetailSheet(
    trade: Trade,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onUpdateTrade: (Trade) -> Unit,
    onDeleteTrade: (Trade) -> Unit,
    onViewFullscreenScreenshot: (String) -> Unit,
    onSaveScreenshotLocally: suspend (Uri) -> String?
) {
    var isEditingNotes by remember { mutableStateOf(false) }
    var setupNotesText by remember(trade.id) { mutableStateOf(trade.setupNotes) }
    var currentScreenshotPath by remember(trade.id) { mutableStateOf(trade.screenshotPath) }
    var isUploadingScreenshot by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Photo picker launcher to replace or upload new chart screenshot
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isUploadingScreenshot = true
                val path = onSaveScreenshotLocally(uri)
                currentScreenshotPath = path
                isUploadingScreenshot = false

                // Persist update immediately
                val updatedTrade = trade.copy(screenshotPath = path)
                onUpdateTrade(updatedTrade)
            }
        }
    }

    val resultColor = when (trade.result) {
        TradeResult.WIN -> NeonEmerald
        TradeResult.LOSS -> CrimsonLoss
        TradeResult.BREAK_EVEN -> BreakEvenAmber
    }

    val resultBg = when (trade.result) {
        TradeResult.WIN -> EmeraldDark
        TradeResult.LOSS -> CrimsonDark
        TradeResult.BREAK_EVEN -> BreakEvenDark
    }

    val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy • HH:mm", Locale.US)
    val formattedDate = sdf.format(Date(trade.dateEpochMillis))

    val confluencePresets = listOf(
        "+ Liquidity Sweep",
        "+ Order Block",
        "+ Fair Value Gap",
        "+ Market Structure Shift",
        "+ 200 EMA Confluence",
        "+ Trendline Retest",
        "+ Risk:Reward 1:3+"
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = DarkBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(DarkBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
                .testTag("trade_detail_sheet")
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = trade.asset,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = TextTertiary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Badges & Metrics Row (Position, Result, PnL)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorderSubtle, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Position Type Badge
                    val isLong = trade.positionType == PositionType.LONG
                    val posColor = if (isLong) NeonEmerald else CrimsonLoss
                    val posBg = if (isLong) EmeraldDark.copy(alpha = 0.7f) else CrimsonDark.copy(alpha = 0.7f)

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(posBg)
                            .border(1.dp, posColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLong) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = posColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = trade.positionType.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = posColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Result Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(resultBg)
                            .border(1.dp, resultColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = trade.result.name.replace("_", " "),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = resultColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // R:R Badge
                    val rrText = if (trade.riskRewardRatio > 0.0) "1:${trade.riskRewardRatio}" else "1:2.0"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF131D2E))
                            .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = rrText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // PnL Amount
                val pnlFormatted = String.format(
                    Locale.US,
                    "%s$%,.2f",
                    if (trade.pnl > 0) "+" else if (trade.pnl < 0) "-" else "",
                    kotlin.math.abs(trade.pnl)
                )
                Text(
                    text = pnlFormatted,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = resultColor,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // FEATURE 1: Dedicated Screenshot Picker & Preview Area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CHART SCREENSHOT & TECHNICAL SETUP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp,
                    fontFamily = FontFamily.Monospace
                )

                if (!currentScreenshotPath.isNullOrBlank()) {
                    Text(
                        text = "CHANGE CHART",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E283D))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!currentScreenshotPath.isNullOrBlank() && File(currentScreenshotPath!!).exists()) {
                val file = File(currentScreenshotPath!!)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .clickable { onViewFullscreenScreenshot(currentScreenshotPath!!) }
                        .testTag("detail_screenshot_preview")
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(file)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Trade Chart Screenshot",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(220.dp)
                    )

                    // Bottom Floating Action Overlay
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TAP TO EXPAND FULLSCREEN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Remove screenshot action
                        IconButton(
                            onClick = {
                                currentScreenshotPath = null
                                onUpdateTrade(trade.copy(screenshotPath = null))
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove chart",
                                tint = CrimsonLoss,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                // Empty state Screenshot Picker area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(vertical = 28.dp, horizontal = 16.dp)
                        .testTag("detail_upload_screenshot_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(DarkSurface)
                                .border(1.dp, CyanAccent.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isUploadingScreenshot) "Uploading screenshot..." else "UPLOAD CHART SCREENSHOT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = 0.8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add chart screenshot showing support/resistance, confluences & triggers",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // FEATURE 2: Dedicated Trading Idea / Setup Notes Text Field (Box)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRADING IDEA / SETUP NOTES BOX",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp,
                    fontFamily = FontFamily.Monospace
                )

                if (!isEditingNotes) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E283D))
                            .clickable { isEditingNotes = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "EDIT NOTES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isEditingNotes) {
                OutlinedTextField(
                    value = setupNotesText,
                    onValueChange = { setupNotesText = it },
                    placeholder = {
                        Text(
                            "Write down your confluence details, trade entry reasons, and execution logic...",
                            color = TextTertiary,
                            fontSize = 13.sp
                        )
                    },
                    minLines = 4,
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("detail_edit_notes_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick confluence tag helpers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    confluencePresets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkBorderSubtle, RoundedCornerShape(6.dp))
                                .clickable {
                                    setupNotesText = if (setupNotesText.isBlank()) {
                                        preset.removePrefix("+ ")
                                    } else {
                                        "$setupNotesText\n• ${preset.removePrefix("+ ")}"
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = preset,
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Save Notes Button
                Button(
                    onClick = {
                        isEditingNotes = false
                        val updatedTrade = trade.copy(setupNotes = setupNotesText.trim())
                        onUpdateTrade(updatedTrade)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("save_notes_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAVE NOTES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            } else {
                // View Mode Notes Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorderSubtle, RoundedCornerShape(12.dp))
                        .clickable { isEditingNotes = true }
                        .padding(14.dp)
                        .testTag("detail_view_notes_box")
                ) {
                    if (setupNotesText.isNotBlank()) {
                        Text(
                            text = setupNotesText,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 19.sp
                        )
                    } else {
                        Text(
                            text = "No trading idea notes written yet. Tap to add confluence details, reasons for entry, and execution logic...",
                            fontSize = 12.sp,
                            color = TextTertiary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Delete Trade Button
            Button(
                onClick = {
                    onDeleteTrade(trade)
                    onDismissRequest()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C1418),
                    contentColor = CrimsonLoss
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonLoss.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("detail_delete_trade_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DELETE TRADE ENTRY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
