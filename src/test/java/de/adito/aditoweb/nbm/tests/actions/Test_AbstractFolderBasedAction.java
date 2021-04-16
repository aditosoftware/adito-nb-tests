package de.adito.aditoweb.nbm.tests.actions;

import de.adito.aditoweb.nbm.tests.actions.create.*;
import org.junit.jupiter.api.Test;
import org.openide.nodes.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AbstractFolderBasedAction}
 *
 * @author s.seemann, 01.04.2021
 */
class Test_AbstractFolderBasedAction
{
  @Test
  void test_enable()
  {
    AbstractFolderBasedAction action = new NewFolderAction();
    _testActionEnable(action);

    action = new NewTestFileAction();
    _testActionEnable(action);
  }

  private void _testActionEnable(AbstractFolderBasedAction action)
  {
    boolean enabled = action.enable0(new Node[]{null, null});
    assertFalse(enabled);

    enabled = action.enable0(new Node[]{null});
    assertFalse(enabled);

    enabled = action.enable0(new Node[]{new AbstractNode(Children.LEAF)});
    assertTrue(enabled);

    enabled = action.enable0(new Node[]{new AbstractNode(Children.LEAF), new AbstractNode(Children.LEAF)});
    assertFalse(enabled);

    enabled = action.enable0(new Node[0]);
    assertFalse(enabled);
  }
}