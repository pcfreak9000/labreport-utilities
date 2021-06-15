/*******************************************************************************
 * Copyright (C) 2020 Roman Borris
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.pcfreak9000.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

import de.pcfreak9000.main.DataTablet;
import de.pcfreak9000.main.DataTablet.DataUsage;
import de.pcfreak9000.main.FunctionTablet.PropagationType;
import de.pcfreak9000.main.Main;
import de.pcfreak9000.main.Tablet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "setf", description = "Sets the content of a data tablet from a csv-file.")
public class SetFromFileCommand implements Callable<Integer> {
    
    private static Map<String, List<String[]>> cache = new HashMap<>();
    
    @Option(names = { "-h", "--help" }, usageHelp = true, description = BaseCommand.HELP_DESC)
    private boolean help;
    
    @Option(names = { "-c",
            "--count" }, defaultValue = "-1", description = "The amount of values. The command will read all lines if not specified.")
    private int count;
    
    @Option(names = { "-o",
            "--offset" }, defaultValue = "0", description = "The line offset, i.e. the amount of lines to ignore.")
    private int lineOffset;
    
    @Option(names = { "-v",
            "--valueIndex" }, defaultValue = "0", description = "The index, zero based, of the value column.")
    private int valueColumn;
    
    @Option(names = { "-e",
            "--errorIndex" }, defaultValue = "1", description = "The index, zero based, of the error column.")
    private int errorColumn;
    
    @Option(names = { "-s",
            "--masd" }, defaultValue = "false", description = "Specify this flag if the mean and the standard deviation of this data set is to be used in calculations instead of doing a computation per individual value.")
    private boolean dataUsageMasd;//masd=Mean and Standard Deviation
    
    @Parameters(paramLabel = "<FILE>", description = "The csv-file from which the tablet is to be filled.", index = "1")
    private Path filepath;
    
    @Parameters(paramLabel = "<TABLET_NAME>", description = "The tablet that is to be filled.", index = "0")
    private String tabletName;
    
    @Option(names = { "--verbose" })
    private boolean verbose;
    
    @Option(names = { "--forceReadFile" })
    private boolean forceReadFile;
    
    @Override
    public Integer call() {
        File file = filepath.toFile();
        if (!file.exists()) {
            System.err.println("File does not exist: '" + file.toString() + "'");
            return Main.CODE_ERROR;
        }
        List<String[]> entries;
        try {
            entries = getEntries();
        } catch (IOException e) {
            System.err.println("Error while reading the file: " + e);
            return Main.CODE_ERROR;
        }
        if (!Main.data.exists(tabletName)) {
            System.out.println("Created the data tablet '" + tabletName + "'.");
            Main.data.createDataTablet(tabletName);
        }
        Tablet ta = Main.data.getTablet(tabletName);
        if (!(ta instanceof DataTablet)) {
            System.err.println("Cannot execute: Tablet '" + tabletName + "' is not a data tablet");
            return Main.CODE_ERROR;
        }
        DataTablet dt = (DataTablet) ta;
        List<String> values = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int index = 0;
        int linesUsed = 0;
        if (verbose) {
            System.out.println("Tab: " + tabletName);
        }
        for (String[] entry : entries) {//TODO Boundary checks, better error messages etc
            if (index < lineOffset) {
                index++;
                continue;
            }
            //value
            String value = entry[valueColumn];
            value = fixNr(value);
            if (verbose) {
                System.out.println("v=" + value);
            }
            values.add(value);
            //error
            String error = errorColumn == -1 ? "0" : entry[errorColumn];
            error = fixNr(error);
            if (verbose) {
                System.out.println("e=" + error);
            }
            errors.add(error);
            //loop stuff
            linesUsed++;
            index++;
            if (count != -1 && linesUsed >= count) {//We are done
                break;
            }
        }
        dt.setValues(values.toArray(String[]::new));
        dt.setErrors(errors.toArray(String[]::new));
        if (dataUsageMasd) {
            dt.setDataUsage(DataUsage.MSD);
        } else {
            dt.setDataUsage(DataUsage.Raw);
        }
        dt.setPreferredPropagation(PropagationType.get(dt.getDataUsage()));
        System.out.println("Filled the tablet '" + tabletName + "' with data from '" + file.toString() + "'.");
        return Main.CODE_NORMAL;
    }
    
    private String fixNr(String in) {
        in = in.replace(',', '.');
        if (in.contains("E")) {
            String[] ar = in.split("E");
            in = "((" + ar[0] + ")*10^(" + Main.evaluator().eval(ar[1]).toString() + "))";
        }
        return in;
    }
    
    //Check if format is supported here? -> another method
    //        if (!error.matches(Main.SUPPORTED_NUMBER_FORMAT_REGEX)) {
    //            //System.err.println("Cell is not matching the supported number format: '" + value + "'");
    //            //return Main.CODE_ERROR;
    //        }
    
    private List<String[]> getEntries() throws IOException {
        List<String[]> ent = cache.get(filepath.toAbsolutePath().toString());
        if (ent == null || forceReadFile) {
            if (cache.size() > 10) {
                cache.clear();
            }
            try (Reader reader = Files.newBufferedReader(filepath)) {
                CSVParser csvParser = new CSVParserBuilder().withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                        .withIgnoreLeadingWhiteSpace(true).withStrictQuotes(false).build();
                CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(0).withCSVParser(csvParser).build();
                ent = csvReader.readAll();
                cache.put(filepath.toAbsolutePath().toString(), ent);
                System.out.println("Parsed the file '" + filepath.toString() + "'.");
            }
        }
        return ent;
    }
}
