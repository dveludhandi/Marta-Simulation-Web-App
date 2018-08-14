package edu.gatech.oad.antlab.person;

/**
 *  A simple class for person 1
 *  returns their name and a
 *  modified string 
 *  
 *  @author Austen
 *  @version 1.1
 */
public class Person1 {
  /** Holds the persons real name */
  private String name;
    /**
   * The constructor, takes in the persons
   * name
   * @param pname the person's real name
   */
  public Person1(String pname) {
    name = pname;
  }
    /**
   * This method should take the string
   * input and return its characters rotated
   * 2 positions.
   * given "gtg123b" it should return
   * "g123bgt".
   *
   * @param input the string to be modified
   * @return the modified string
   */
  private String calc(String input) {
    char[] modified = input.toCharArray();
      for (int i = 0; i < 2; i++) {
        char temp = modified[0];
        int j;
        for (j = 0; j < modified.length - 1; j++) {
          modified[j] = modified[j + 1];
        }
        modified[j] = temp;
      }
      return (new String(modified));
  }
  
  /**
   * Return a string rep of this object
   * that varies with an input string
   *
   * @param input the varying string
   * @return the string representing the 
   *         object
   */
  public String toString(String input) {
    return name + calc(input);
  }

}
