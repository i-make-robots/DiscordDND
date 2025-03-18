package com.marginallyclever.discorddnd.dndactions;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseTest {
    @Test
    public void testSanitizeMessage() {
        test("(3d6+2d8)-(4d10+5)");
        test("1d6+2d8-(3d10+4)");
        test("4d6k1+2");
    }

    private void test(String input) {
        Pattern p = Pattern.compile("\\(([^\\)]+)\\)|^[^\\(\\)]+$");
        Matcher m = p.matcher(input);

        List<String> groups = new ArrayList<>();
        while (m.find()) {
            groups.add(m.group());
        }

        System.out.println(groups);
    }
}
