package ru.itis.jlab;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//@Parameters(separators = "=")
public class User {
    @Parameter(names = {"name", "Name"})
    private String name;

    @Parameter(names = {"surname", "Surname"})
    private String surname;

    @Parameter(names = {"passport", "passportNumber"})
    private String passportNumber;

    @Parameter(names = {"age"})
    private int age;


    @Parameter(names = {"dateOfIssue", "issue"}, converter = TimeConverter.class)
    private Date dateOfIssue;

    private String pattern = "dd.mm.yyyy";

    public byte[] getBytes() {
        Field[] fields = User.class.getDeclaredFields();
        StringBuilder allParameters = new StringBuilder();
        try {
            for (Field f : fields) {
                Object v = f.get(this);
                String s;
                if (f.getName().intern() == "pattern") continue;
                if (f.getName().intern() == "dateOfIssue") {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, new Locale("en"));
                    s = simpleDateFormat.format(v);
                } else {
                    s = v.toString();
                }
                allParameters.append(f.getName() + " " + s + " ");
            }
            String allParametersString = allParameters.toString();
            return allParametersString.substring(0, allParametersString.length() - 1).getBytes();
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException();
        }
    }

    public Map<String, Object> getParameters() {
        Map<String, Object> map = new HashMap();
        Field[] fields = User.class.getDeclaredFields();
        try {
            for (Field f :
                    fields) {
                Object v = f.get(this);
                map.put(f.getName(), v);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException();
        }
        return map;
    }
}

class TimeConverter implements IStringConverter<Date> {
    private String pattern = "dd.mm.yyyy";

    @Override
    public Date convert(String value) {
        try {
            return new SimpleDateFormat(pattern, new Locale("en")).parse(value);
        } catch (ParseException e) {
            throw new IllegalArgumentException("невозможно распарсить дату");
        }
    }
}
