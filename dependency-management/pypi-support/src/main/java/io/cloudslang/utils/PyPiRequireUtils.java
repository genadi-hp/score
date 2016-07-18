package io.cloudslang.utils;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 18/07/2016.
 */
public class PyPiRequireUtils {
    public static final String REQUIRE_EQ = "==";
    public static final String REQUIRE_GT = ">=";
    public static final String REQUIRE_LS = "<=";

    public static String getLibraryNameFromRequirement(String requirement) {
        String processStr = requirement.trim();
        String condition = getRequirementCondition(processStr);
        if(condition == null) {
            return requirement.trim();
        }
        return processStr.substring(0, processStr.indexOf(condition)).trim();
    }

    public static String getRequirementCondition(String requirement) {
        if(requirement.contains(REQUIRE_EQ)) {
            return REQUIRE_EQ;
        }
        if(requirement.contains(REQUIRE_GT)) {
            return REQUIRE_GT;
        }
        if(requirement.contains(REQUIRE_LS)) {
            return REQUIRE_LS;
        }
        return null;
    }

    public static String getLibraryVersionFromRequirement(String requirement) {
        String processStr = requirement.trim();
        String condition = getRequirementCondition(processStr);
        if(condition == null) {
            return null;
        }
        return processStr.substring(processStr.indexOf(condition) + condition.length()).trim();
    }
}
