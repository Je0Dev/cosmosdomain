package com.papel.imdb_clone.enums;

//Enum for ethnicity
public enum Ethnicity {

    // Regional Ethnicities
    AFRICAN("African"),
    ARAB("Arab"),
    ASIAN("Asian"),
    CAUCASIAN("Caucasian"),
    HISPANIC("Hispanic"),
    INDIAN("Indian"),
    INDIGENOUS_AMERICAN("Indigenous American"),
    INDIGENOUS_AUSTRALIAN("Indigenous Australian"),
    PACIFIC_ISLANDER("Pacific Islander"),
    
    // Nationalities
    AMERICAN("American"),
    AUSTRALIAN("Australian"),
    BRITISH("British"),
    CANADIAN("Canadian"),
    CHINESE("Chinese"),
    FRENCH("French"),
    GERMAN("German"),
    INDIAN_NATIONAL("Indian"),
    IRISH("Irish"),
    ITALIAN("Italian"),
    JAPANESE("Japanese"),
    KOREAN("Korean"),
    MALAYSIAN("Malaysian"),
    MEXICAN("Mexican"),
    NEW_ZEALAND("New Zealander"),
    RUSSIAN("Russian"),
    SCOTTISH("Scottish"),
    SPANISH("Spanish"),
    SWEDISH("Swedish"),
    
    // Additional
    MIXED("Mixed"),
    OTHER("Other"),
    UNKNOWN("Unknown"),
    CAUCASOID("Caucasoid");

    private final String label;

    //Ethnicity constructor with label which means the name of the ethnicity
    Ethnicity(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    //get name which means the name of the enum
    public String getName() {
        return name();
    }

    /**
     * Converts a string label to an Ethnicity enum value.
     * Handles various input formats and provides case-insensitive matching.
     * 
     * @param input The input string to convert
     * @return Matching Ethnicity enum value
     * @throws IllegalArgumentException if no matching ethnicity is found
     */
    public static Ethnicity fromLabel(String input) {
        if (input == null || input.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        // Normalize input
        String normalized = input.trim().toLowerCase();
        
        // First try direct label match (case-insensitive)
        for (Ethnicity e : values()) {
            if (e.label.toLowerCase().equals(normalized)) {
                return e;
            }
        }
        
        //  common variations-aliases
        switch (normalized) {
            case "australian":
            case "aus":
                return AUSTRALIAN;
            case "new_zealand":
            case "new zealand":
            case "nz":
                return NEW_ZEALAND;
            case "scot":
            case "scotland":
                return SCOTTISH;
            case "malay":
                return MALAYSIAN;
            case "eire":
                return IRISH;
        }
        
        // Try matching enum name style (UPPER_UNDERSCORE)
        String nameLike = normalized.toUpperCase()
            .replace('-', '_')
            .replace(' ', '_');
            
        try {
            return Ethnicity.valueOf(nameLike);
        } catch (IllegalArgumentException e) {
            // Continue to next check
        }
        
        // Try partial matches
        for (Ethnicity e : values()) {
            if (e.label.toLowerCase().contains(normalized) || 
                normalized.contains(e.label.toLowerCase())) {
                return e;
            }
        }

       // Return unknown if no match found from the above values
       return UNKNOWN;
    }

}
