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
import java.util.List;

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

@Command(name = "setf", description = "Sets the content of a data tablet from a file.")
public class SetFromFileCommand implements Runnable {
    
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
    
    @Parameters(paramLabel = "<FILE>", description = "The file from which the tablet is to be filled.", index = "1")
    private Path filepath;
    
    @Parameters(paramLabel = "<TABLET_NAME>", description = "The tablet that is to be filled.", index = "0")
    private String tabletName;
    
    @Override
    public void run() {
        File file = filepath.toFile();
        if (!file.exists()) {//TODO does picocli solve this already?
            System.out.println("File does not exist: '" + file.toString() + "'");
            return;
        }
        List<String[]> entries;
        //TODO maybe automatically find out about the tables contents or something
        try (Reader reader = Files.newBufferedReader(filepath)) {
            CSVParser csvParser = new CSVParserBuilder().withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                    .withIgnoreLeadingWhiteSpace(true).withStrictQuotes(false).build();
            CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(lineOffset).withCSVParser(csvParser)
                    .build();
            entries = csvReader.readAll();
        } catch (IOException e) {
            System.out.println("Error while reading the file: " + e);
            return;
        }
        Tablet ta = Main.data.getTablet(tabletName);
        if (!(ta instanceof DataTablet)) {
            System.out.println("Cannot execute: Tablet '" + tabletName + "' is not a data tablet");
            return;
        }
        DataTablet dt = (DataTablet) ta;
        List<String> values = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int index = 0;
        for (String[] entry : entries) {//TODO Boundary checks, better error messages etc
            String value = entry[valueColumn];
            value = value.replace(',', '.');
            if (!value.matches(Main.SUPPORTED_NUMBER_FORMAT_REGEX)) {//Put this in a method!!
                System.out.println("Cell is not matching the supported number format: '" + value + "'");
                return;
            }
            values.add(value);
            String error = entry[errorColumn];//errorColumn == -1 -> error is zero?
            error = error.replace(',', '.');
            if (!error.matches(Main.SUPPORTED_NUMBER_FORMAT_REGEX)) {
                System.out.println("Cell is not matching the supported number format: '" + value + "'");
                return;
            }
            errors.add(error);
            index++;
            if (count != -1 && index >= count) {//We are done
                break;
            }
        }
        dt.setValues(values.toArray(String[]::new));
        dt.setErrors(errors.toArray(String[]::new));
        if (dataUsageMasd) {
            dt.setDataUsage(DataUsage.MeanAndStandardDeviation);
        } else {
            dt.setDataUsage(DataUsage.Raw);
        }
        dt.setPreferredPropagation(PropagationType.get(dt.getDataUsage()));
        System.out.println("Successfully read the file '" + file.toString() + "'");
    }
}
