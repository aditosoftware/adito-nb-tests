package de.adito.aditoweb.nbm.tests.internal;

/**
 * Exception, if Node-JS cannot be found.
 *
 * @author s.seemann, 17.03.2021
 */
public class NoNodeJSException extends RuntimeException
{
  public NoNodeJSException()
  {
    super("Cannot find NodeJS, maybe plugin is not installed?");
  }
}
