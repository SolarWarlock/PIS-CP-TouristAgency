package ui.screens.tours

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import data.model.LookupItem
import data.model.Tour // Импорт модели
import ui.common.BrandBlue

@Composable
fun TourEditDialog(
    tourToEdit: Tour? = null, // Если null - создание, иначе редактирование
    types: List<LookupItem>,
    partners: List<LookupItem>,
    onDismiss: () -> Unit,
    // ID (null если новый), TypeID, PartnerID, ...
    onSave: (Int?, Int, Int, String, String, String, String, String) -> Unit
) {
    // Парсинг дат из строки "01.01.2025 - 10.01.2025" (грязный хак для UI, лучше хранить даты в модели)
    val splitDates = tourToEdit?.dates?.split(" - ")
    val initialStart = splitDates?.getOrNull(0)?.split(".")?.let { "${it[2]}-${it[1]}-${it[0]}" } ?: "2025-06-01"
    val initialEnd = splitDates?.getOrNull(1)?.split(".")?.let { "${it[2]}-${it[1]}-${it[0]}" } ?: "2025-06-10"

    var destination by remember { mutableStateOf(tourToEdit?.destination ?: "") }
    var cost by remember { mutableStateOf(tourToEdit?.cost?.toString() ?: "") }
    var startDate by remember { mutableStateOf(initialStart) }
    var endDate by remember { mutableStateOf(initialEnd) }
    var description by remember { mutableStateOf(tourToEdit?.description ?: "") }

    // Пытаемся найти текущий тип и партнера по имени (так как в модели Tour нет ID)
    var selectedType by remember { mutableStateOf(types.find { it.name == tourToEdit?.typeName }) }
    var selectedPartner by remember { mutableStateOf(partners.find { it.name == tourToEdit?.partnerName }) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    if (tourToEdit == null) "Новый тур" else "Редактирование тура",
                    style = MaterialTheme.typography.headlineSmall,
                    color = BrandBlue
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Направление") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                DropdownSelector("Тип тура", types, selectedType) { selectedType = it }
                Spacer(Modifier.height(8.dp))

                DropdownSelector("Партнер", partners, selectedPartner) { selectedPartner = it }
                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Начало") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("Конец") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Стоимость") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Отмена") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedType != null && selectedPartner != null) {
                                onSave(
                                    tourToEdit?.id, // ID тура (null если новый)
                                    selectedType!!.id,
                                    selectedPartner!!.id,
                                    destination,
                                    startDate,
                                    endDate,
                                    cost,
                                    description
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownSelector(
    label: String,
    items: List<LookupItem>,
    selected: LookupItem?,
    onSelect: (LookupItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.name) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}