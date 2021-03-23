package cz.palda97.lpclient.model.pipeline

import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.entities.pipeline.ConfigInput
import cz.palda97.lpclient.model.entities.pipeline.ConfigInputFactory
import org.junit.Test

import org.junit.Assert.*

class ConfigFactoryTest
    : PowerMockTest() {

    @Test
    fun parseAutocomplete() {
        val html = stringFromFile("e-sparqlEndpoint.html")
        val factory = ConfigInputFactory(html, COMPONENT_ID)
        val configInputs = factory.parse()
        assertNotNull(configInputs)
        configInputs!!
        val id = "headerAccept"
        val autocomplete = configInputs.find { it.id == id }
        assertEquals(
            ConfigInput("MIME type", ConfigInput.Type.EDIT_TEXT, id, COMPONENT_ID),
            autocomplete
        )
    }

    @Test
    fun parseTabular() {
        val factory = ConfigInputFactory(TABULAR_UV, COMPONENT_ID)
        val configInputs = factory.parse()
        assertNotNull(configInputs)
        //TODO()
    }

    companion object {

        private const val COMPONENT_ID = "http://localhost:8080/resources/pipelines/1604082676059/component/a0db-a8d9"

        private const val TABULAR_UV =
            "<lp-dialog-control-config lp-dialog=\"dialog\" lp-application=\"application\"></lp-dialog-control-config>\n" +
                    "<div>\n" +
                    "\t<div layout=\"row\" layout-align=\"space-between\">\n" +
                    "\t\t<div layout=\"column\">\n" +
                    "\t\t\t<md-input-container class=\"md-block\" ng-hide=\"dialog.tableType.hide\">\n" +
                    "\t\t\t\t<label>Table type</label>\n" +
                    "\t\t\t\t<md-select ng-model=\"dialog.tableType.value\"\n" +
                    "\t\t\t\t\tng-disabled=\"dialog.tableType.disabled || dialog.tableType.inherit\">\n" +
                    "\t\t\t\t\t<md-option value=\"CSV\">CSV</md-option>\n" +
                    "\t\t\t\t\t<md-option value=\"DBF\">DBF</md-option>\n" +
                    "\t\t\t\t\t<md-option value=\"XLS\">XLS</md-option>\n" +
                    "\t\t\t\t</md-select>\n" +
                    "\t\t\t</md-input-container>\n" +
                    "\t\t\t<md-input-container class=\"md-block\" ng-hide=\"dialog.baseUri.hide\">\n" +
                    "\t\t\t\t<label>Resource URI base</label>\n" +
                    "\t\t\t\t<input ng-model=\"dialog.baseUri.value\"\n" +
                    "                       ng-disabled=\"dialog.baseUri.disabled || dialog.baseUri.inherit\">\n" +
                    "            </md-input-container>\n" +
                    "\t\t\t\t<md-input-container class=\"md-block\" ng-hide=\"dialog.keyColumn.hide\">\n" +
                    "\t\t\t\t\t<label>Key column</label>\n" +
                    "\t\t\t\t\t<input ng-model=\"dialog.keyColumn.value\"\n" +
                    "                       ng-disabled=\"dialog.keyColumn.disabled || dialog.keyColumn.inherit\">\n" +
                    "            </md-input-container>\n" +
                    "\t\t\t\t\t<md-input-container class=\"md-block\" ng-hide=\"dialog.encoding.hide\">\n" +
                    "\t\t\t\t\t\t<label>Encoding</label>\n" +
                    "\t\t\t\t\t\t<input ng-model=\"dialog.encoding.value\"\n" +
                    "                       ng-disabled=\"dialog.encoding.disabled || dialog.encoding.inherit\">\n" +
                    "            </md-input-container>\n" +
                    "\t\t\t\t\t\t<md-input-container class=\"md-block\" ng-hide=\"dialog.rowsLimit.hide\">\n" +
                    "\t\t\t\t\t\t\t<label>Rows limit</label>\n" +
                    "\t\t\t\t\t\t\t<input ng-model=\"dialog.rowsLimit.value\"\n" +
                    "                       ng-disabled=\"dialog.rowsLimit.disabled || dialog.rowsLimit.inherit\">\n" +
                    "            </md-input-container>\n" +
                    "\t\t\t\t\t\t\t<md-input-container class=\"md-block\" ng-hide=\"dialog.rowClass.hide\">\n" +
                    "\t\t\t\t\t\t\t\t<label>Class for a row entity</label>\n" +
                    "\t\t\t\t\t\t\t\t<input ng-model=\"dialog.rowClass.value\"\n" +
                    "                       ng-disabled=\"dialog.rowClass.disabled || dialog.rowClass.inherit\">\n" +
                    "            </md-input-container>\n" +
                    "\t\t</div>\n" +
                    "\t\t<div layout=\"column\">\n" +
                    "\t\t\t<md-input-container class=\"md-block\" ng-hide=\"dialog.quote.hide\">\n" +
                    "\t\t\t\t<label>Quote char</label>\n" +
                    "\t\t\t\t<input ng-model=\"dialog.quote.value\"\n" +
                    "                       ng-disabled=\"dialog.quote.disabled || dialog.quote.inherit\">\n" +
                    "            </md-input-container>\n" +
                    "\t\t\t\t<md-input-container class=\"md-block\" ng-hide=\"dialog.delimeter.hide\">\n" +
                    "\t\t\t\t\t<label>Delimiter char</label>\n" +
                    "\t\t\t\t\t<input ng-model=\"dialog.delimeter.value\"\n" +
                    "                       ng-disabled=\"dialog.delimeter.disabled || dialog.delimeter.inherit\">\n" +
                    "            </md-input-container>\n" +
                    "\t\t\t\t\t<md-input-container class=\"md-block\" ng-hide=\"dialog.linesToIgnore.hide\">\n" +
                    "\t\t\t\t\t\t<label>Skip n first lines</label>\n" +
                    "\t\t\t\t\t\t<input ng-model=\"dialog.linesToIgnore.value\"\n" +
                    "                       ng-disabled=\"dialog.linesToIgnore.disabled || dialog.linesToIgnore.inherit\">\n" +
                    "            </md-input-container>\n" +
                    "\t\t\t\t\t\t<md-switch ng-model=\"dialog.hasHeader.value\"\n" +
                    "\t\t\t\t\t\t\tng-disabled=\"dialog.hasHeader.disabled || dialog.hasHeader.inherit\"\n" +
                    "\t\t\t\t\t\t\tng-hide=\"dialog.hasHeader.hide\">Has header</md-switch>\n" +
                    "\t\t</div>\n" +
                    "\t\t<div layout=\"column\">\n" +
                    "\t\t\t<md-input-container class=\"md-block\" ng-hide=\"dialog.sheetName.hide\">\n" +
                    "\t\t\t\t<label>Sheet name</label>\n" +
                    "\t\t\t\t<input ng-model=\"dialog.sheetName.value\"\n" +
                    "                       ng-disabled=\"dialog.sheetName.disabled || dialog.sheetName.inherit\">\n" +
                    "            </md-input-container>\n" +
                    "\t\t\t\t<md-switch ng-model=\"dialog.stripHeader.value\"\n" +
                    "\t\t\t\t\tng-disabled=\"dialog.stripHeader.disabled || dialog.stripHeader.inherit\"\n" +
                    "\t\t\t\t\tng-hide=\"dialog.stripHeader.hide\">Strip header for nulls</md-switch>\n" +
                    "\t\t\t\t<md-switch ng-model=\"dialog.xlsAdvancedParser.value\"\n" +
                    "\t\t\t\t\tng-disabled=\"dialog.xlsAdvancedParser.disabled || dialog.xlsAdvancedParser.inherit\"\n" +
                    "\t\t\t\t\tng-hide=\"dialog.xlsAdvancedParser.hide\">Use advanced parser for 'double'</md-switch>\n" +
                    "\t\t\t\t<md-switch ng-model=\"dialog.useDataFormatter.value\"\n" +
                    "\t\t\t\t\tng-disabled=\"dialog.useDataFormatter.disabled || dialog.useDataFormatter.inherit\"\n" +
                    "\t\t\t\t\tng-hide=\"dialog.useDataFormatter.hide\">Format numbers with DataFormatter</md-switch>\n" +
                    "\t\t</div>\n" +
                    "\t</div>\n" +
                    "\t<div layout=\"row\" layout-align=\"space-between\">\n" +
                    "\t\t<div layout=\"column\">\n" +
                    "\t\t\t<md-switch ng-model=\"dialog.generateNew.value\"\n" +
                    "\t\t\t\tng-disabled=\"dialog.generateNew.disabled || dialog.generateNew.inherit\"\n" +
                    "\t\t\t\tng-hide=\"dialog.generateNew.hide\">Full column mapping</md-switch>\n" +
                    "\t\t\t<md-switch ng-model=\"dialog.tableSubject.value\"\n" +
                    "\t\t\t\tng-disabled=\"dialog.tableSubject.disabled || dialog.tableSubject.inherit\"\n" +
                    "\t\t\t\tng-hide=\"dialog.tableSubject.hide\">Generate subject for table</md-switch>\n" +
                    "\t\t\t<md-switch ng-model=\"dialog.ignoreMissingColumn.value\"\n" +
                    "\t\t\t\tng-disabled=\"dialog.ignoreMissingColumn.disabled || dialog.ignoreMissingColumn.inherit\"\n" +
                    "\t\t\t\tng-hide=\"dialog.ignoreMissingColumn.hide\">Ignore missing columns</md-switch>\n" +
                    "\t\t\t<md-switch ng-model=\"dialog.ignoreBlankCell.value\"\n" +
                    "\t\t\t\tng-disabled=\"dialog.ignoreBlankCell.disabled || dialog.ignoreBlankCell.inherit\"\n" +
                    "\t\t\t\tng-hide=\"dialog.ignoreBlankCell.hide\">Ignore blank cells</md-switch>\n" +
                    "\t\t</div>\n" +
                    "\t\t<div layout=\"column\">\n" +
                    "\t\t\t<md-switch ng-model=\"dialog.autoAsString.value\"\n" +
                    "\t\t\t\tng-disabled=\"dialog.autoAsString.disabled || dialog.autoAsString.inherit\"\n" +
                    "\t\t\t\tng-hide=\"dialog.autoAsString.hide\">Auto type as string</md-switch>\n" +
                    "\t\t\t<md-switch ng-model=\"dialog.staticRowCounter.value\"\n" +
                    "\t\t\t\tng-disabled=\"dialog.staticRowCounter.disabled || dialog.staticRowCounter.inherit\"\n" +
                    "\t\t\t\tng-hide=\"dialog.staticRowCounter.hide\">Use static row counter</md-switch>\n" +
                    "\t\t\t<md-switch ng-model=\"dialog.tableClass.value\"\n" +
                    "\t\t\t\tng-disabled=\"dialog.tableClass.disabled || dialog.tableClass.inherit\" ng-hide=\"dialog.tableClass.hide\">\n" +
                    "\t\t\t\tGenerate table/row class</md-switch>\n" +
                    "\t\t\t<md-switch ng-model=\"dialog.advancedKey.value\"\n" +
                    "\t\t\t\tng-disabled=\"dialog.advancedKey.disabled || dialog.advancedKey.inherit\"\n" +
                    "\t\t\t\tng-hide=\"dialog.advancedKey.hide\">Advances key column</md-switch>\n" +
                    "\t\t</div>\n" +
                    "\t\t<div layout=\"column\">\n" +
                    "\t\t\t<md-switch ng-model=\"dialog.generateLabels.value\"\n" +
                    "\t\t\t\tng-disabled=\"dialog.generateLabels.disabled || dialog.generateLabels.inherit\"\n" +
                    "\t\t\t\tng-hide=\"dialog.generateLabels.hide\">Generate labels</md-switch>\n" +
                    "\t\t\t<md-switch ng-model=\"dialog.generateRowTriple.value\"\n" +
                    "\t\t\t\tng-disabled=\"dialog.generateRowTriple.disabled || dialog.generateRowTriple.inherit\"\n" +
                    "\t\t\t\tng-hide=\"dialog.generateRowTriple.hide\">Generate row column</md-switch>\n" +
                    "\t\t\t<md-switch ng-model=\"dialog.trimString.value\"\n" +
                    "\t\t\t\tng-disabled=\"dialog.trimString.disabled || dialog.trimString.inherit\" ng-hide=\"dialog.trimString.hide\">\n" +
                    "\t\t\t\tRemove trailing spaces</md-switch>\n" +
                    "\t\t</div>\n" +
                    "\t</div>\n" +
                    "\t<md-tabs md-dynamic-height md-border-bottom>\n" +
                    "\t\t<md-tab label=\"Simple\">\n" +
                    "\t\t\t<div ng-hide=\"dialog.column.disabled || dialog.column.inherit || dialog.column.hide\">\n" +
                    "\t\t\t\t<section style=\"background: #f7f7f7; margin-top: 1em;\" layout-gt-sm=\"row\"\n" +
                    "\t\t\t\t\tng-repeat=\"item in dialog.column.value\">\n" +
                    "\t\t\t\t\t<md-input-container>\n" +
                    "\t\t\t\t\t\t<label>Column</label>\n" +
                    "\t\t\t\t\t\t<input ng-model=\"item.name.value\">\n" +
                    "                    </md-input-container>\n" +
                    "\t\t\t\t\t\t<md-input-container>\n" +
                    "\t\t\t\t\t\t\t<label>Type</label>\n" +
                    "\t\t\t\t\t\t\t<md-select ng-model=\"item.type.value\">\n" +
                    "\t\t\t\t\t\t\t\t<md-option value=\"String\">String</md-option>\n" +
                    "\t\t\t\t\t\t\t\t<md-option value=\"Integer\">Integer</md-option>\n" +
                    "\t\t\t\t\t\t\t\t<md-option value=\"Long\">Long</md-option>\n" +
                    "\t\t\t\t\t\t\t\t<md-option value=\"Double\">Double</md-option>\n" +
                    "\t\t\t\t\t\t\t\t<md-option value=\"Float\">Float</md-option>\n" +
                    "\t\t\t\t\t\t\t\t<md-option value=\"Date\">Date</md-option>\n" +
                    "\t\t\t\t\t\t\t\t<md-option value=\"Boolean\">Boolean</md-option>\n" +
                    "\t\t\t\t\t\t\t\t<md-option value=\"gYear\">gYear</md-option>\n" +
                    "\t\t\t\t\t\t\t\t<md-option value=\"Decimal\">Decimal</md-option>\n" +
                    "\t\t\t\t\t\t\t\t<md-option value=\"Auto\">Auto</md-option>\n" +
                    "\t\t\t\t\t\t\t</md-select>\n" +
                    "\t\t\t\t\t\t</md-input-container>\n" +
                    "\t\t\t\t\t\t<md-input-container style=\"width:3em\">\n" +
                    "\t\t\t\t\t\t\t<label>Lang</label>\n" +
                    "\t\t\t\t\t\t\t<input ng-model=\"item.lang.value\">\n" +
                    "                    </md-input-container>\n" +
                    "\t\t\t\t\t\t\t<md-input-container style=\"width:5em\">\n" +
                    "\t\t\t\t\t\t\t\t<label>DBF types</label>\n" +
                    "\t\t\t\t\t\t\t\t<md-switch ng-model=\"item.typeFromDbf.value\" aria-label=\"Use DBF types\"></md-switch>\n" +
                    "\t\t\t\t\t\t\t</md-input-container>\n" +
                    "\t\t\t\t\t\t\t<md-input-container flex>\n" +
                    "\t\t\t\t\t\t\t\t<label>Property IRI</label>\n" +
                    "\t\t\t\t\t\t\t\t<input ng-model=\"item.uri.value\">\n" +
                    "                    </md-input-container>\n" +
                    "\t\t\t\t\t\t\t\t<button ng-click=\"onDelete(dialog.column.value, \$index)\" class=\"md-icon-button md-accent md-button md-ink-ripple\">\n" +
                    "                        <md-icon class=\"material-icons lp-icon\">clear</md-icon>\n" +
                    "                    </button>\n" +
                    "\t\t\t\t</section>\n" +
                    "\t\t\t\t<button ng-click=\"onAddColumn()\" class=\"md-icon-button md-primary md-button md-ink-ripple\">\n" +
                    "                    <md-icon class=\"material-icons lp-icon\">add_circle_outline</md-icon>\n" +
                    "                </button>\n" +
                    "\t\t\t</div>\n" +
                    "\t\t</md-tab>\n" +
                    "\t\t<md-tab label=\"Advanced\">\n" +
                    "\t\t\t<div\n" +
                    "\t\t\t\tng-hide=\"dialog.advancedMapping.disabled || dialog.advancedMapping.inherit || dialog.advancedMapping.hide\">\n" +
                    "\t\t\t\t<section style=\"background: #f7f7f7; margin-top: 1em;\" layout-gt-sm=\"row\"\n" +
                    "\t\t\t\t\tng-repeat=\"item in dialog.advancedMapping.value\">\n" +
                    "\t\t\t\t\t<md-input-container>\n" +
                    "\t\t\t\t\t\t<label>URI</label>\n" +
                    "\t\t\t\t\t\t<input ng-model=\"item.uri.value\">\n" +
                    "                    </md-input-container>\n" +
                    "\t\t\t\t\t\t<md-input-container flex>\n" +
                    "\t\t\t\t\t\t\t<label>Template</label>\n" +
                    "\t\t\t\t\t\t\t<input ng-model=\"item.template.value\">\n" +
                    "                    </md-input-container>\n" +
                    "\t\t\t\t\t\t\t<button ng-click=\"onDelete(dialog.advancedMapping.value, \$index)\" class=\"md-icon-button md-accent md-button md-ink-ripple\">\n" +
                    "                        <md-icon class=\"material-icons lp-icon\">clear</md-icon>\n" +
                    "                    </button>\n" +
                    "\t\t\t\t</section>\n" +
                    "\t\t\t\t<button ng-click=\"onAddAdvancedColumn()\" class=\"md-icon-button md-primary md-button md-ink-ripple\">\n" +
                    "                    <md-icon class=\"material-icons lp-icon\">add_circle_outline</md-icon>\n" +
                    "                </button>\n" +
                    "\t\t\t</div>\n" +
                    "\t\t</md-tab>\n" +
                    "\t\t<md-tab label=\"XLS Mapping\">\n" +
                    "\t\t\t<div ng-hide=\"dialog.namedCell.disabled || dialog.namedCell.inherit || dialog.namedCell.hide\">\n" +
                    "\t\t\t\t<section style=\"background: #f7f7f7; margin-top: 1em;\" layout-gt-sm=\"row\"\n" +
                    "\t\t\t\t\tng-repeat=\"item in dialog.namedCell.value\">\n" +
                    "\t\t\t\t\t<md-input-container>\n" +
                    "\t\t\t\t\t\t<label>Name</label>\n" +
                    "\t\t\t\t\t\t<input ng-model=\"item.name.value\">\n" +
                    "                    </md-input-container>\n" +
                    "\t\t\t\t\t\t<md-input-container flex>\n" +
                    "\t\t\t\t\t\t\t<label>Row</label>\n" +
                    "\t\t\t\t\t\t\t<input ng-model=\"item.rowNumber.value\">\n" +
                    "                    </md-input-container>\n" +
                    "\t\t\t\t\t\t\t<md-input-container flex>\n" +
                    "\t\t\t\t\t\t\t\t<label>Column</label>\n" +
                    "\t\t\t\t\t\t\t\t<input ng-model=\"item.columnNumber.value\">\n" +
                    "                    </md-input-container>\n" +
                    "\t\t\t\t\t\t\t\t<button ng-click=\"onDelete(dialog.namedCell.value, \$index)\" class=\"md-icon-button md-accent md-button md-ink-ripple\">\n" +
                    "                        <md-icon class=\"material-icons lp-icon\">clear</md-icon>\n" +
                    "                    </button>\n" +
                    "\t\t\t\t</section>\n" +
                    "\t\t\t\t<button ng-click=\"onAddNamedCell()\" class=\"md-icon-button md-primary md-button md-ink-ripple\">\n" +
                    "                    <md-icon class=\"material-icons lp-icon\">add_circle_outline</md-icon>\n" +
                    "                </button>\n" +
                    "\t\t\t</div>\n" +
                    "\t\t</md-tab>\n" +
                    "\t</md-tabs>\n" +
                    "</div>"
    }
}