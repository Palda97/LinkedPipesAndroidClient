package cz.palda97.lpclient.model.pipeline

import cz.palda97.lpclient.model.entities.pipeline.DialogJsFactory
import org.junit.Test

import org.junit.Assert.*

class DialogJsFactoryTest {

    @Test
    fun parseHttpGet() {
        val dialogJs = DialogJsFactory(HTTP_GET, COMPONENT_ID).parse()
        assertNotNull(dialogJs)
        val expectedMap = mapOf(
            "fileName" to "fileName",
            "utf8Redirect" to "utf8Redirect",
            "userAgent" to "userAgent",
            "uri" to "fileUri",
            "hardRedirect" to "hardRedirect",
            "encodeUrl" to "encodeUrl"
        )
        assertEquals(expectedMap, dialogJs!!.map)
        assertEquals("http://plugins.linkedpipes.com/ontology/e-httpGetFile#", dialogJs.namespace)
        assertEquals("http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUri", dialogJs.getFullPropertyName("uri"))

        val uriControl = "http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUriControl"
        assertEquals("uri", dialogJs.fullControlNameToReverse(uriControl))
    }

    @Test
    fun parseTabular() {
        val dialogJs = DialogJsFactory(TABULAR_UV, COMPONENT_ID).parse()
        assertNotNull(dialogJs)
    }

    companion object {

        const val COMPONENT_ID = "http://localhost:8080/resources/pipelines/1604082676059/component/a0db-a8d9"

        private const val TABULAR_UV = "define([\"jsonld\"], function (jsonld) {\n" +
                "    \"use strict\";\n" +
                "\n" +
                "    const DESC = {\n" +
                "        \"\$namespace\": \"http://plugins.linkedpipes.com/ontology/t-tabularUv#\",\n" +
                "        \"\$type\": \"Configuration\",\n" +
                "        \"\$options\": {\n" +
                "            \"\$predicate\": \"auto\",\n" +
                "            \"\$control\": \"auto\"\n" +
                "        },\n" +
                "        \"baseUri\": {\n" +
                "            \"\$type\": \"str\",\n" +
                "            \"\$label\": \"Key column\"\n" +
                "        },\n" +
                "        \"keyColumn\": {\n" +
                "            \"\$type\": \"str\",\n" +
                "            \"\$label\": \"Key column\"\n" +
                "        },\n" +
                "        \"column\": {\n" +
                "            \"\$label\": \"Mapping\",\n" +
                "            \"\$array\": true,\n" +
                "            \"\$object\": {\n" +
                "                \"\$type\": \"ColumnInfo\",\n" +
                "                \"name\" : {\n" +
                "                    \"\$type\": \"str\"\n" +
                "                },\n" +
                "                \"type\": {\n" +
                "                    \"\$type\": \"str\"\n" +
                "                },\n" +
                "                \"lang\": {\n" +
                "                    \"\$type\": \"str\"\n" +
                "                },\n" +
                "                \"typeFromDbf\": {\n" +
                "                    \"\$type\": \"bool\"\n" +
                "                },\n" +
                "                \"uri\" : {\n" +
                "                    \"\$type\": \"str\"\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"advancedMapping\": {\n" +
                "            \"\$label\": \"Advanced mapping\",\n" +
                "            \"\$array\": true,\n" +
                "            \"\$object\": {\n" +
                "                \"\$type\": \"AdvancedMapping\",\n" +
                "                \"uri\" : {\n" +
                "                    \"\$type\": \"str\"\n" +
                "                },\n" +
                "                \"template\" : {\n" +
                "                    \"\$type\": \"str\"\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"namedCell\": {\n" +
                "            \"\$label\": \"XLS cell mapping\",\n" +
                "            \"\$array\": true,\n" +
                "            \"\$object\": {\n" +
                "                \"\$type\": \"NamedCell\",\n" +
                "                \"name\" : {\n" +
                "                    \"\$type\": \"str\"\n" +
                "                },\n" +
                "                \"rowNumber\" : {\n" +
                "                    \"\$type\": \"int\"\n" +
                "                },\n" +
                "                \"columnNumber\" : {\n" +
                "                    \"\$type\": \"int\"\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"quote\": {\n" +
                "            \"\$type\": \"str\",\n" +
                "            \"\$label\": \"Quote char\"\n" +
                "        },\n" +
                "        \"delimeter\": {\n" +
                "            \"\$type\": \"str\",\n" +
                "            \"\$label\": \"Delimiter char\"\n" +
                "        },\n" +
                "        \"linesToIgnore\": {\n" +
                "            \"\$type\": \"int\",\n" +
                "            \"\$label\": \"Skip n first lines\"\n" +
                "        },\n" +
                "        \"encoding\": {\n" +
                "            \"\$type\": \"str\",\n" +
                "            \"\$label\": \"Encoding\"\n" +
                "        },\n" +
                "        \"rowsLimit\": {\n" +
                "            \"\$type\": \"int\",\n" +
                "            \"\$label\": \"Rows limit\"\n" +
                "        },\n" +
                "        \"tableType\": {\n" +
                "            \"\$type\": \"str\",\n" +
                "            \"\$label\": \"Table type\"\n" +
                "        },\n" +
                "        \"hasHeader\": {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Has header\"\n" +
                "        },\n" +
                "        \"generateNew\": {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Full column mapping\"\n" +
                "        },\n" +
                "        \"ignoreBlankCell\": {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Ignore blank cells\"\n" +
                "        },\n" +
                "        \"advancedKey\": {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Advances key column\"\n" +
                "        },\n" +
                "        \"rowClass\": {\n" +
                "            \"\$type\": \"str\",\n" +
                "            \"\$label\": \"Class for a row entity\"\n" +
                "        },\n" +
                "        \"sheetName\": {\n" +
                "            \"\$type\": \"str\",\n" +
                "            \"\$label\": \"Sheet name\"\n" +
                "        },\n" +
                "        \"staticRowCounter\": {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Use static row counter\"\n" +
                "        },\n" +
                "        \"rowTriple\": {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Generate row column\"\n" +
                "        },\n" +
                "        \"tableSubject\": {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Generate subject for table\"\n" +
                "        },\n" +
                "        \"autoAsString\": {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Auto type as string\"\n" +
                "        },\n" +
                "        \"tableClass\": {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Generate table/row class\"\n" +
                "        },\n" +
                "        \"generateLabels\": {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Generate labels\"\n" +
                "        },\n" +
                "        \"stripHeader\": {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Strip header for nulls\"\n" +
                "        },\n" +
                "        \"trimString\": {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Remove trailing spaces\"\n" +
                "        },\n" +
                "        \"xlsAdvancedParser\": {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Use advanced parser for 'double'\"\n" +
                "        },\n" +
                "        \"ignoreMissingColumn\": {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Ignore missing columns\"\n" +
                "        },\n" +
                "        \"generateRowTriple\": {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Generate row column\"\n" +
                "        },\n" +
                "        \"useDataFormatter\" : {\n" +
                "            \"\$type\": \"bool\",\n" +
                "            \"\$label\": \"Format numbers with DataFormatter\"\n" +
                "        }\n" +
                "    };\n" +
                "\n" +
                "    function controller(\$scope, \$service) {\n" +
                "\n" +
                "        if (\$scope.dialog === undefined) {\n" +
                "            \$scope.dialog = {};\n" +
                "        }\n" +
                "\n" +
                "        \$scope.onAddColumn = function () {\n" +
                "            \$scope.dialog.column.value.push({\n" +
                "                'name': {'value': ''},\n" +
                "                'type': {'value': ''},\n" +
                "                'lang': {'value': ''},\n" +
                "                'typeFromDbf': {'value': false},\n" +
                "                'uri': {'value': ''}\n" +
                "            });\n" +
                "        };\n" +
                "\n" +
                "        \$scope.onAddAdvancedColumn = function () {\n" +
                "            \$scope.dialog.advancedMapping.value.push({\n" +
                "                'uri': {'value': ''},\n" +
                "                'template': {'value': ''}\n" +
                "            });\n" +
                "        };\n" +
                "\n" +
                "        \$scope.onAddNamedCell = function () {\n" +
                "            \$scope.dialog.namedCell.value.push({\n" +
                "                'name': {'value': ''},\n" +
                "                'rowNumber': {'value': 0},\n" +
                "                'columnNumber': {'value': 0}\n" +
                "            });\n" +
                "        };\n" +
                "\n" +
                "        \$scope.onDelete = function (data, index) {\n" +
                "            data.splice(index, 1);\n" +
                "        };\n" +
                "\n" +
                "        const dialogManager = \$service.v1.manager(DESC, \$scope.dialog);\n" +
                "\n" +
                "        \$service.onStore = function () {\n" +
                "            dialogManager.save();\n" +
                "        };\n" +
                "\n" +
                "        function i18_str(value) {\n" +
                "            if (value === undefined) {\n" +
                "                return undefined;\n" +
                "            }\n" +
                "            if (Array.isArray(value)) {\n" +
                "                const result = [];\n" +
                "                value.forEach((item) => {\n" +
                "                    result.push(item[\"@value\"]);\n" +
                "                });\n" +
                "                return result;\n" +
                "            } else if (value[\"@value\"]) {\n" +
                "                return value[\"@value\"];\n" +
                "            } else {\n" +
                "                return value;\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        dialogManager.load();\n" +
                "\n" +
                "        // Load rowsClass to rowClass if rowClass is not set.\n" +
                "        if (\$scope.dialog.rowClass.value === undefined) {\n" +
                "            const triples = jsonld.q.getGraph(\$service.config.instance, null);\n" +
                "            const prefix = 'http://plugins.linkedpipes.com/ontology/t-tabularUv#';\n" +
                "            const resource = jsonld.t.getResourceByType(\n" +
                "                triples, prefix + 'Configuration');\n" +
                "            \$scope.dialog.rowClass.value =\n" +
                "                i18_str(jsonld.r.getString(resource, prefix + 'rowsClass'));\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    controller.\$inject = ['\$scope', '\$service'];\n" +
                "    return controller;\n" +
                "});"

        const val HTTP_GET = "define([], function () {\n" +
                "    \"use strict\";\n" +
                "\n" +
                "    const DESC = {\n" +
                "        \"\$namespace\" : \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#\",\n" +
                "        \"\$type\": \"Configuration\",\n" +
                "        \"uri\" : {\n" +
                "            \"\$type\" : \"str\",\n" +
                "            \"\$property\" : \"fileUri\",\n" +
                "            \"\$control\": \"fileUriControl\",\n" +
                "            \"\$label\" : \"File URL\"\n" +
                "        },\n" +
                "        \"fileName\" : {\n" +
                "            \"\$type\" : \"str\",\n" +
                "            \"\$property\" : \"fileName\",\n" +
                "            \"\$control\": \"fileNameControl\",\n" +
                "            \"\$label\" : \"File name\"\n" +
                "        },\n" +
                "        \"hardRedirect\" : {\n" +
                "            \"\$type\" : \"bool\",\n" +
                "            \"\$property\" : \"hardRedirect\",\n" +
                "            \"\$control\": \"hardRedirectControl\",\n" +
                "            \"\$label\" : \"Force to follow redirects\"\n" +
                "        },\n" +
                "        \"encodeUrl\" : {\n" +
                "            \"\$type\" : \"bool\",\n" +
                "            \"\$property\" : \"encodeUrl\",\n" +
                "            \"\$control\": \"encodeUrlControl\",\n" +
                "            \"\$label\" : \"Encode input URL\"\n" +
                "        },\n" +
                "        \"userAgent\" : {\n" +
                "            \"\$type\" : \"str\",\n" +
                "            \"\$property\" : \"userAgent\",\n" +
                "            \"\$control\": \"userAgentControl\",\n" +
                "            \"\$label\" : \"User agent\"\n" +
                "        },\n" +
                "        \"utf8Redirect\": {\n" +
                "          \"\$type\" : \"bool\",\n" +
                "          \"\$property\" : \"utf8Redirect\",\n" +
                "          \"\$control\": \"utf8RedirectControl\",\n" +
                "          \"\$label\" : \"Use UTF-8 encoding for redirect\"\n" +
                "        }\n" +
                "    };\n" +
                "\n" +
                "    function controller(\$scope, \$service) {\n" +
                "\n" +
                "        if (\$scope.dialog === undefined) {\n" +
                "            \$scope.dialog = {};\n" +
                "        }\n" +
                "\n" +
                "        const dialogManager = \$service.v1.manager(DESC, \$scope.dialog);\n" +
                "\n" +
                "        \$service.onStore = function () {\n" +
                "            dialogManager.save();\n" +
                "        };\n" +
                "\n" +
                "        dialogManager.load();\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    controller.\$inject = ['\$scope', '\$service'];\n" +
                "    return controller;\n" +
                "});\n"
    }
}