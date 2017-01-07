public class PermissionConfig {
    /**
     * Used for first time authentication at a headless server which may be at a remote location.
     */
    private String oneTimeAuthorizationKey = createRandomKey();

    /**
     * @return Returns the one-time auth key
     */
    public String getOneTimeAuthorizationKey() {
        return oneTimeAuthorizationKey;
    }

    /**
     * @param oneTimeAuthorizationKey Sets the one-time authorization key to this String
     */
    public void setOneTimeAuthorizationKey(String oneTimeAuthorizationKey) {
        this.oneTimeAuthorizationKey = oneTimeAuthorizationKey;
    }

    /**
     * @return Builds a random key on the priniciple stated below:
     * Create a random object, and a list of possible characters
     * Populate possible characters with A-Z, a-z, and 2-9.
     * Remove all 'l', 'I' and 'O' characters
     * Create a StringBuilder called codeBuilder and do this:
     * Loop 20 times
     * If 1 is not zero, and you have reached a character that is a multiple of four, add a dash
     * Dash or not, append something random from possibleCharacters.
     * Once complete return the StringBuilder codeBuilder.toString();
     */
    private static String createRandomKey() {
        SecureRandom random = new SecureRandom();
        List<Character> possibleCharacters = new ArrayList<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            possibleCharacters.add(c);
        }
        for (char c = 'a'; c <= 'z'; c++) {
            possibleCharacters.add(c);
        }
        for (char c = '2'; c <= '9'; c++) {
            possibleCharacters.add(c);
        }

        possibleCharacters.remove(Character.valueOf('l'));
        possibleCharacters.remove(Character.valueOf('I'));
        possibleCharacters.remove(Character.valueOf('O'));

        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            if ((i != 0) && (i % 4 == 0)) {
                codeBuilder.append("-");
            }
            codeBuilder.append(possibleCharacters.get(random.nextInt(possibleCharacters.size())));
        }
        return codeBuilder.toString();
    }
}