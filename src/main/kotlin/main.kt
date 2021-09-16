import java.io.File
import kotlin.math.abs

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
    val inputFilename = args.getOrElse(0) { "input.txt" }
    val technologyMap = generateMapFromLinesOfFile(inputFilename)
    val hammingLimit = args.getOrElse(2) { "2" }.toInt()
    processHammingDistance(technologyMap, hammingLimit)

    val outputFilename = args.getOrElse(1) { "output.sql" }
    val sqlFile = File(outputFilename)
    sqlFile.writeText("")
    technologyMap.keys.toSortedSet().forEach {
        val insertStatement = technologyMap[it]!!.getInsertSqlStatement()
        sqlFile.appendText("${insertStatement}\n")
        println(insertStatement)
    }
}

fun processHammingDistance(technologyMap: Map<String, ParsedTechnology>, hammingLimit: Int) {
    val keys = technologyMap.keys.sorted()
    for (i in keys.indices) {
        val firstKey = keys[i]
        for (j in i+1 until keys.size) {
            val secondKey = keys[j]
            var hamming = abs(secondKey.length - firstKey.length)
            for (letterIndex in firstKey.indices) {
                if (letterIndex >= secondKey.length) break;
                if (firstKey[letterIndex] != secondKey[letterIndex]) hamming++;
            }
            if (hamming <= hammingLimit) {
                technologyMap[firstKey]!!.addSimilar(technologyMap[secondKey]!!.name)
                technologyMap[secondKey]!!.addSimilar(technologyMap[firstKey]!!.name)
                println("$firstKey's lowercase hamming distance to $secondKey is $hamming, they are oddly similar")
            }
        }
    }
}

fun generateMapFromLinesOfFile(filename: String) : Map<String, ParsedTechnology> {
    val technologyMap = HashMap<String, ParsedTechnology>()
    File(filename).forEachLine {
        val key = it.lowercase()
        if (technologyMap.containsKey(key)) {
            technologyMap[key]!!.multipleOccurrences = true
        } else {
            technologyMap[key] = ParsedTechnology(it)
        }
    }
    return technologyMap
}

data class ParsedTechnology(val name: String, val similarTechnologies: MutableSet<String> = HashSet()) {
    companion object {
        const val MULTIPLE_OCCURRENCES_COMMENT = " -- you should review this name, multiple occurrences were found"
        const val SIMILAR_TECHNOLOGIES_COMMENT_PREFIX = " -- this technology is suspiciously similar to the following technologies: "
    }
    var multipleOccurrences = false

    fun getInsertSqlStatement() : String {
        val multipleOccurrencesComment = if (multipleOccurrences) MULTIPLE_OCCURRENCES_COMMENT else ""
        val similarTechnologiesComment = if (similarTechnologies.isNotEmpty()) "$SIMILAR_TECHNOLOGIES_COMMENT_PREFIX${similarTechnologies.joinToString(separator = ", ")}" else ""
        return "insert into technology (name) values ('$name');$multipleOccurrencesComment$similarTechnologiesComment"
    }

    fun addSimilar(similar: String) {
        similarTechnologies.add(similar)
    }
}

