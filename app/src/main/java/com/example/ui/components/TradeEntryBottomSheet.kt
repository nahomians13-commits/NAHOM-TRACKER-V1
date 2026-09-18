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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.PositionType
import com.example.data.TradeResult
import com.example.ui.theme.BreakEvenAmber
import com.example.ui.theme.BreakEvenDark
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonLoss
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeEntryBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onSaveTrade: (
        asset: String,
        positionType: PositionType,
        result: TradeResult,
        pnl: Double,
        riskRewardRatio: Double,
        dateEpochMillis: Long,
        screenshotPath: String?,
        setupNotes: String
    ) -> Unit,
    onSaveScreenshotLocally: suspend (Uri) -> String?,
    preselectedDateMillis: Long = System.currentTimeMillis()
) {
    var asset by remember { mutableStateOf("") }
    var positionType by remember { mutableStateOf(PositionType.LONG) }
    var result by remember { mutableStateOf(TradeResult.WIN) }
    var pnlInput by remember { mutableStateOf("250.00") }
    var riskRewardInput by remember { mutableStateOf("2.5") }
    var setupNotes by remember { mutableStateOf("") }
    var localScreenshotPath by remember { mutableStateOf<String?>(null) }
    var isSavingScreenshot by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Photo picker launcher (Android Photo Picker, zero dangerous permissions)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isSavingScreenshot = true
                val path = onSaveScreenshotLocally(uri)
                localScreenshotPath = path
                isSavingScreenshot = false
            }
        }
    }

    val assetPresets = listOf("EUR/USD", "GBP/USD", "XAU/USD", "BTC/USD", "US30", "NAS100", "ETH/USD")

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
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LOG NEW TRADE",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Record execution, setup rationale & chart confluences",
                        fontSize = 12.sp,
                        color = TextSecondary
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

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Currency Pair / Asset Field
            Text(
                text = "CURRENCY PAIR / ASSET",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = asset,
                onValueChange = { asset = it },
                placeholder = { Text("e.g. XAU/USD, EUR/USD, BTC/USD", color = TextTertiary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                ),
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
                    .testTag("asset_input_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Asset Presets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                assetPresets.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (asset.equals(preset, ignoreCase = true)) CyanAccent.copy(alpha = 0.2f) else DarkSurfaceElevated)
                            .border(
                                1.dp,
                                if (asset.equals(preset, ignoreCase = true)) CyanAccent else DarkBorderSubtle,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { asset = preset }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = preset,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (asset.equals(preset, ignoreCase = true)) CyanAccent else TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Position Type (LONG vs SHORT)
            Text(
                text = "POSITION TYPE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // LONG Button
                val isLong = positionType == PositionType.LONG
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLong) EmeraldDark else DarkSurfaceElevated)
                        .border(
                            1.5.dp,
                            if (isLong) NeonEmerald else DarkBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { positionType = PositionType.LONG }
                        .padding(vertical = 12.dp)
                        .testTag("position_long_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isLong) NeonEmerald else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LONG (BUY)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLong) NeonEmerald else TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // SHORT Button
                val isShort = positionType == PositionType.SHORT
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isShort) CrimsonDark else DarkSurfaceElevated)
                        .border(
                            1.5.dp,
                            if (isShort) CrimsonLoss else DarkBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { positionType = PositionType.SHORT }
                        .padding(vertical = 12.dp)
                        .testTag("position_short_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isShort) CrimsonLoss else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SHORT (SELL)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isShort) CrimsonLoss else TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Trade Result (WIN / LOSS / BREAK-EVEN)
            Text(
                text = "RESULT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // WIN
                ResultButton(
                    label = "WIN",
                    isSelected = result == TradeResult.WIN,
                    activeColor = NeonEmerald,
                    activeBg = EmeraldDark,
                    modifier = Modifier.weight(1f),
                    testTag = "result_win_button",
                    onClick = {
                        result = TradeResult.WIN
                        if (pnlInput.startsWith("-")) pnlInput = pnlInput.removePrefix("-")
                        if (pnlInput == "0" || pnlInput.isEmpty()) pnlInput = "250.00"
                    }
                )

                // LOSS
                ResultButton(
                    label = "LOSS",
                    isSelected = result == TradeResult.LOSS,
                    activeColor = CrimsonLoss,
                    activeBg = CrimsonDark,
                    modifier = Modifier.weight(1f),
                    testTag = "result_loss_button",
                    onClick = {
                        result = TradeResult.LOSS
                        if (!pnlInput.startsWith("-") && pnlInput != "0") {
                            pnlInput = "-${pnlInput.removePrefix("+")}"
                        }
                    }
                )

                // BREAK-EVEN
                ResultButton(
                    label = "B/E",
                    isSelected = result == TradeResult.BREAK_EVEN,
                    activeColor = BreakEvenAmber,
                    activeBg = BreakEvenDark,
                    modifier = Modifier.weight(1f),
                    testTag = "result_be_button",
                    onClick = {
                        result = TradeResult.BREAK_EVEN
                        pnlInput = "0.00"
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. PnL Amount
            Text(
                text = "PROFIT / LOSS AMOUNT ($)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = pnlInput,
                onValueChange = { pnlInput = it },
                placeholder = { Text("e.g. 350.00 or -120.00", color = TextTertiary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = when (result) {
                        TradeResult.WIN -> NeonEmerald
                        TradeResult.LOSS -> CrimsonLoss
                        TradeResult.BREAK_EVEN -> BreakEvenAmber
                    },
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pnl_input_field")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Risk-to-Reward Ratio (R:R)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RISK : REWARD RATIO (R:R)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "1 : $riskRewardInput",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = riskRewardInput,
                onValueChange = { riskRewardInput = it },
                placeholder = { Text("e.g. 2.5 for 1:2.5 RR", color = TextTertiary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
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
                    .testTag("rr_input_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick R:R Presets
            val rrPresets = listOf("1.5", "2.0", "2.5", "3.0", "4.0")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rrPresets.forEach { preset ->
                    val isSelected = riskRewardInput == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CyanAccent.copy(alpha = 0.2f) else DarkSurfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) CyanAccent else DarkBorderSubtle,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { riskRewardInput = preset }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1:$preset",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) CyanAccent else TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Screenshot Attachment
            Text(
                text = "CHART SCREENSHOT ATTACHMENT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (localScreenshotPath != null && File(localScreenshotPath!!).exists()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, CyanAccent, RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(localScreenshotPath!!))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Attached Chart",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )

                    // Remove button
                    IconButton(
                        onClick = { localScreenshotPath = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove screenshot",
                            tint = CrimsonLoss,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Replace button
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.8f))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Replace Image",
                            fontSize = 11.sp,
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(vertical = 18.dp, horizontal = 16.dp)
                        .testTag("upload_screenshot_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Attach Screenshot",
                            tint = CyanAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isSavingScreenshot) "Attaching screenshot..." else "Upload Chart Screenshot",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Attach technical analysis, order blocks & entry trigger",
                                fontSize = 11.sp,
                                color = TextTertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Trading Idea / Notes Box
            Text(
                text = "TRADING IDEA / SETUP NOTES BOX",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = setupNotes,
                onValueChange = { setupNotes = it },
                placeholder = {
                    Text(
                        "Type notes detailing trade setup, confluence details, trade entry reasons, market structure, liquidity sweeps, and execution logic...",
                        color = TextTertiary,
                        fontSize = 13.sp
                    )
                },
                minLines = 4,
                maxLines = 8,
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
                    .testTag("setup_notes_input_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Confluence Helper Chips
            val entryConfluencePresets = listOf(
                "+ Liquidity Sweep",
                "+ Order Block",
                "+ Fair Value Gap",
                "+ Market Structure Shift",
                "+ 200 EMA Retest",
                "+ Trendline Break",
                "+ Risk:Reward 1:3"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                entryConfluencePresets.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(6.dp))
                            .clickable {
                                setupNotes = if (setupNotes.isBlank()) {
                                    tag.removePrefix("+ ")
                                } else {
                                    "$setupNotes\n• ${tag.removePrefix("+ ")}"
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            val isFormValid = asset.isNotBlank()
            Button(
                onClick = {
                    if (isFormValid) {
                        val parsedPnl = when (result) {
                            TradeResult.WIN -> kotlin.math.abs(pnlInput.toDoubleOrNull() ?: 250.0)
                            TradeResult.LOSS -> -kotlin.math.abs(pnlInput.toDoubleOrNull() ?: 100.0)
                            TradeResult.BREAK_EVEN -> 0.0
                        }

                        val parsedRR = riskRewardInput.toDoubleOrNull() ?: 2.0

                        onSaveTrade(
                            asset,
                            positionType,
                            result,
                            parsedPnl,
                            parsedRR,
                            preselectedDateMillis,
                            localScreenshotPath,
                            setupNotes
                        )
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonEmerald,
                    contentColor = Color.Black,
                    disabledContainerColor = DarkBorder,
                    disabledContentColor = TextTertiary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_trade_submit_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SAVE TRADE TO JOURNAL",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
private fun ResultButton(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    activeBg: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) activeBg else DarkSurfaceElevated)
            .border(
                1.5.dp,
                if (isSelected) activeColor else DarkBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) activeColor else TextSecondary,
            fontFamily = FontFamily.Monospace
        )
    }
}
