import java.io.File

/**
 * Reads a text file line by line, adds the lines to a Map using the lowercase format of the lines as keys.
 * The values are then printed as SQL insert statements insert into technology(name) values('*content of line*');
 * If multiple lines are found with the same keys a comment is printed on the end of the SQL statement, to indicate
 * that it should be reviewed by a person if it is spelled correctly.
 *
 * First command line argument: path to input file
 * Second command line argument: path to output file, if not present, defaults to "build/insert-technologies.sql"
 *
 * Run example: gradlew run --args="\"C:\Users\Marcell Szukács\Desktop\technologies.txt\""
 */
fun main(args: Array<String>) {
    val inputFilename = args[0]
    val technologyMap = generateMapFromLinesOfFile(inputFilename)

    var outputFilename = "build/insert-technologies.sql"
    if (args.size > 1) {
        outputFilename = args[1]
    }
    val sqlFile = File(outputFilename)
    sqlFile.writeText("")
    technologyMap.keys.toSortedSet().forEach {
        val insertStatement = technologyMap[it]!!.getInsertSqlStatement()
        sqlFile.appendText("${insertStatement}\n")
        println(insertStatement)
    }
}

fun generateMapFromLinesOfFile(filename: String) : Map<String, ParsedTechnology> {
    val technologyMap = HashMap<String, ParsedTechnology>()
    File(filename).forEachLine {
        val key = it.lowercase()
        if (technologyMap.containsKey(key)) {
            technologyMap[key]!!.multipleOccurrances = true
        } else {
            technologyMap.put(it.lowercase(), ParsedTechnology(it))
        }
    }
    return technologyMap
}

data class ParsedTechnology(val name: String) {
    companion object {
        const val MULTIPLE_OCCURRENCES_COMMENT = " -- you should review this name, multiple occurrences were found"
    }
    var multipleOccurrances = false

    fun getInsertSqlStatement() : String {
        return "insert into technology(name) values ('${name}');${if (multipleOccurrances) MULTIPLE_OCCURRENCES_COMMENT else ""}"
    }
}

