package me.reimnop.d4f.customevents;

import java.util.List;
import java.util.Random;

// Technoblade quote generator, my tribute to Technoblade. RIP
public final class TechnobladeQuoteFactory {
    private TechnobladeQuoteFactory() {}

    private static final Random random = new Random();

    private static final List<String> quotes = List.of(
            "Blood for the blood god!",
            "Technoblade never dies!",
            "Let's drop-kick some children!",
            "Officer, I drop-kicked them in self defense!",
            "Rest in peace, Technoblade",
            "so long nerds"
    );

    public static String getRandomQuote() {
        return quotes.get(random.nextInt(quotes.size()));
    }
}
