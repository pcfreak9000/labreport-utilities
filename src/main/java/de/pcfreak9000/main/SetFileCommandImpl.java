package de.pcfreak9000.main;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

import de.pcfreak9000.command.Argument;
import de.pcfreak9000.command.Arguments;
import de.pcfreak9000.command.ICommand;
import de.pcfreak9000.main.DataTablet.DataType;

public class SetFileCommandImpl implements ICommand {
    
    @Override
    public boolean checkArguments(List<Argument> args) {
        if (args.size() < 2) {
            System.out.println("Cannot execute: Malformed arguments");
            return false;
        }
        return true;
    }
    
    @Override
    public void execute(List<Argument> args) {
        String filename = args.get(0).getArgument();
        Path filepath = Paths.get(filename);
        File file = filepath.toFile();
        if (!file.exists()) {
            System.out.println("File does not exist: '" + file.toString() + "'");
            return;
        }
        List<Argument> fileOptions = Arguments.commandOptions(args, "-", 0);
        Map<String, String> mapped = Arguments.mapArguments(fileOptions, null, false);
        int skipLines = 0;
        int skipRows = 0;
        int linesCount = -1;
        int rowsCount = -1;
        int nextArg = 1 + fileOptions.size();
        if (mapped.get("-skipl") != null) {
            skipLines = Integer.parseInt(mapped.get("-skipl"));
        }
        if (mapped.get("-skipr") != null) {
            skipRows = Integer.parseInt(mapped.get("-skipr"));
        }
        if (mapped.get("-countl") != null) {
            linesCount = Integer.parseInt(mapped.get("-countl"));
        }
        if (mapped.get("-countr") != null) {
            rowsCount = Integer.parseInt(mapped.get("-countr"));
        }
        List<String[]> entries;
        //TODO maybe automatically find out about the tables contents or something
        try (Reader reader = Files.newBufferedReader(filepath)) {
            CSVParser csvParser = new CSVParserBuilder().withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                    .withIgnoreLeadingWhiteSpace(true).withStrictQuotes(false).build();
            CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(skipLines).withCSVParser(csvParser)
                    .build();
            entries = csvReader.readAll();
        } catch (IOException e) {
            System.out.println("Error while reading the file: " + e);
            return;
        }
        Tablet ta = Main.data.getTablet(args.get(nextArg).getArgument());
        if (!(ta instanceof DataTablet)) {
            System.out.println("Cannot execute: Tablet '" + args.get(nextArg).getArgument() + "' is not a data tablet");
            return;
        }
        DataTablet dt = (DataTablet) ta;
        List<String> values = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < (linesCount == -1 ? entries.size() : linesCount); i++) {
            int k = 0;
            for (int j = skipRows; j < skipRows + (rowsCount == -1 ? entries.get(i).length : rowsCount); j++) {
                String cell = entries.get(i)[j];
                if (cell != null) {
                    cell = cell.replace(',', '.');
                    if (!cell.matches(Main.SUPPORTED_NUMBER_FORMAT_REGEX)) {
                        System.out.println("Cell is not matching the supported number format: '" + cell + "'");
                        return;
                    } //big oof here:
                    if (k == 0) {
                        values.add(cell);
                        k++;
                    } else if (k == 1) {
                        errors.add(cell);
                    }
                }
            }
        }
        dt.setValues(values.toArray(String[]::new));
        dt.setErrors(errors.toArray(String[]::new));
        List<String> tabletOptions = Arguments.commandOptions(args, "-", nextArg).stream().map(Argument::getArgument)
                .collect(Collectors.toList());
        if (tabletOptions.contains("-stat")) {
            dt.setType(DataType.RAW_STATISTICAL);
        } else {
            dt.setType(DataType.RAW_MULTI);
        }
        System.out.println("Successfully read the file '" + file.toString() + "'");
    }
}
