package de.blau.android.easyedit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import de.blau.android.App;
import de.blau.android.LayerUtils;
import de.blau.android.Logic;
import de.blau.android.Main;
import de.blau.android.Map;
import de.blau.android.R;
import de.blau.android.TestUtils;
import de.blau.android.layer.LayerType;
import de.blau.android.osm.Node;
import de.blau.android.osm.Way;
import de.blau.android.prefs.AdvancedPrefDatabase;
import de.blau.android.prefs.Preferences;
import de.blau.android.tasks.Note;
import de.blau.android.tasks.Task;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SimpleActionsTest {

    Context              context = null;
    AdvancedPrefDatabase prefDB  = null;
    Main                 main    = null;
    UiDevice             device  = null;
    Map                  map     = null;
    Logic                logic   = null;
    Preferences          prefs   = null;

    @Rule
    public ActivityTestRule<Main> mActivityRule = new ActivityTestRule<>(Main.class);

    /**
     * Pre-test setup
     */
    @Before
    public void setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        main = mActivityRule.getActivity();
        prefs = new Preferences(context);
        LayerUtils.removeImageryLayers(context);
        prefs.enableSimpleActions(true);
        main.runOnUiThread(() -> main.showSimpleActionsButton());
        map = main.getMap();
        map.setPrefs(main, prefs);
        TestUtils.grantPermissons(device);
        TestUtils.dismissStartUpDialogs(device, main);
        logic = App.getLogic();
        logic.deselectAll();
        TestUtils.loadTestData(main, "test2.osm");
        App.getTaskStorage().reset();
        TestUtils.stopEasyEdit(main);
    }

    /**
     * Post-test teardown
     */
    @After
    public void teardown() {
        TestUtils.stopEasyEdit(main);
        TestUtils.zoomToNullIsland(logic, map);
        App.getTaskStorage().reset();
    }

    /**
     * Create a new Node
     */
    // @SdkSuppress(minSdkVersion = 26)
    @Test
    public void newNode() {
        map.getDataLayer().setVisible(true);
        TestUtils.zoomToLevel(device, main, 21);
        TestUtils.unlock(device);
        TestUtils.clickSimpleButton(device);
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.menu_add_node), true, false));
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.simple_add_node)));
        TestUtils.clickAtCoordinates(device, map, 8.3893454, 47.3901898, true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_nodeselect)));

        Node node = App.getLogic().getSelectedNode();
        assertNotNull(node);
        assertTrue(node.getOsmId() < 0);

        TestUtils.clickUp(device);
    }

    /**
     * Create a new Node, then undo
     */
    // @SdkSuppress(minSdkVersion = 26)
    @Test
    public void newNodeUndo() {
        map.getDataLayer().setVisible(true);
        TestUtils.zoomToLevel(device, main, 21);
        TestUtils.unlock(device);
        TestUtils.clickSimpleButton(device);
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.menu_add_node), true, false));
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.simple_add_node)));
        TestUtils.clickAtCoordinates(device, map, 8.3893454, 47.3901898, true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_nodeselect)));

        Node node = App.getLogic().getSelectedNode();
        assertNotNull(node);
        assertTrue(node.getOsmId() < 0);

        // now undo, this should end the node selection mode
        assertTrue(TestUtils.clickMenuButton(device, context.getString(R.string.undo), false, false));
        TestUtils.clickText(device, false, context.getString(R.string.okay), true); // click away tip
        assertTrue(TestUtils.textGone(device, context.getString(R.string.actionmode_nodeselect), 2000));
        assertNull(App.getLogic().getSelectedNode());
    }

    
    /**
     * Create a new way from menu and clicks at two more locations and finishing via home button
     */
    // @SdkSuppress(minSdkVersion = 26)
    @Test
    public void newWay() {
        map.getDataLayer().setVisible(true);
        TestUtils.zoomToLevel(device, main, 21);
        TestUtils.unlock(device);
        TestUtils.clickSimpleButton(device);
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.menu_add_way), true, false));
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.simple_add_way)));
        TestUtils.clickAtCoordinates(device, map, 8.3893454, 47.3901898, true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_createpath), 1000));
        TestUtils.clickAtCoordinates(device, map, 8.3895763, 47.3901374, true);
        TestUtils.sleep();
        TestUtils.clickAtCoordinates(device, map, 8.3896274, 47.3902424, true);
        TestUtils.sleep();
        TestUtils.clickAtCoordinates(device, map, 8.3897000, 47.3903500, true);
        TestUtils.sleep();
        // undo last addition
        Assert.assertTrue(TestUtils.clickMenuButton(device, context.getString(R.string.undo), false, false));
        TestUtils.sleep();
        TestUtils.clickButton(device, device.getCurrentPackageName() + ":id/simpleButton", true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.tag_form_untagged_element)));
        TestUtils.clickHome(device, true);
        Way way = App.getLogic().getSelectedWay();
        assertNotNull(way);
        assertTrue(way.getOsmId() < 0);
        assertEquals(3, way.nodeCount());
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_wayselect)));
        TestUtils.clickUp(device);
    }
    
    /**
     * Create a new closed way from menu and clicks at two more locations and finishing via home button
     */
    // @SdkSuppress(minSdkVersion = 26)
    @Test
    public void newClosedWay() {
        map.getDataLayer().setVisible(true);
        TestUtils.zoomToLevel(device, main, 21);
        TestUtils.unlock(device);
        TestUtils.clickSimpleButton(device);
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.menu_add_way), true, false));
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.simple_add_way)));
        TestUtils.clickAtCoordinates(device, map, 8.3893454, 47.3901898, true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_createpath), 1000));
        TestUtils.clickAtCoordinates(device, map, 8.3895763, 47.3901374, true);
        TestUtils.sleep();
        TestUtils.clickAtCoordinates(device, map, 8.3896274, 47.3902424, true);
        TestUtils.sleep();
        TestUtils.clickAtCoordinates(device, map, 8.3897000, 47.3903500, true);
        TestUtils.sleep();
        TestUtils.clickAtCoordinates(device, map, 8.3893454, 47.3901898, true);
        TestUtils.sleep();
        TestUtils.clickButton(device, device.getCurrentPackageName() + ":id/simpleButton", true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.tag_form_untagged_element)));
        TestUtils.clickHome(device, true);
        Way way = App.getLogic().getSelectedWay();
        assertNotNull(way);
        assertTrue(way.getOsmId() < 0);
        assertEquals(5, way.nodeCount());
        assertTrue(way.isClosed());
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_wayselect)));
        TestUtils.clickUp(device);
    }

    /**
     * Create a new way from menu and with snapping turned off
     */
    // @SdkSuppress(minSdkVersion = 26)
    @Test
    public void newWayNoSnap() {
        map.getDataLayer().setVisible(true);
        TestUtils.zoomToLevel(device, main, 21);
        TestUtils.unlock(device);
        TestUtils.clickSimpleButton(device);
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.menu_add_way), true, false));
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.simple_add_way)));
        
        assertTrue(TestUtils.clickMenuButton(device, context.getString(R.string.menu_snap), false, false));
        
        TestUtils.clickAtCoordinates(device, map, 8.3893454, 47.3901898, true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_createpath), 1000));
        TestUtils.clickAtCoordinates(device, map, 8.3895763, 47.3901374, true);
        TestUtils.sleep();
        TestUtils.clickAtCoordinates(device, map, 8.3896274, 47.3902424, true);
        TestUtils.sleep();
        TestUtils.clickAtCoordinates(device, map, 8.3897000, 47.3903500, true);
        TestUtils.sleep();
        TestUtils.clickAtCoordinates(device, map, 8.3893454, 47.3901898, true);
        TestUtils.sleep();
        assertTrue(TestUtils.clickMenuButton(device, context.getString(R.string.menu_snap), false, false));
        TestUtils.clickButton(device, device.getCurrentPackageName() + ":id/simpleButton", true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.tag_form_untagged_element)));
        TestUtils.clickHome(device, true);
        Way way = App.getLogic().getSelectedWay();
        assertNotNull(way);
        assertTrue(way.getOsmId() < 0);
        assertEquals(5, way.nodeCount());
        assertFalse(way.isClosed());
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_wayselect)));
        TestUtils.clickUp(device);
    }

    
    /**
     * Create a new way and completely undo it
     */
    // @SdkSuppress(minSdkVersion = 26)
    @Test
    public void newWayUndo() {
        int prevChanges = App.getDelegator().getApiElementCount();
 App.getDelegator().getApiStorage().logStorage();        
        map.getDataLayer().setVisible(true);
        TestUtils.zoomToLevel(device, main, 21);
        TestUtils.unlock(device);
        TestUtils.clickSimpleButton(device);
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.menu_add_way), true, false));
        final String addWayString = context.getString(R.string.simple_add_way);
        assertTrue(TestUtils.findText(device, false, addWayString));
        TestUtils.clickAtCoordinates(device, map, 8.3893454, 47.3901898, true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_createpath), 1000));
        TestUtils.clickAtCoordinates(device, map, 8.3895763, 47.3901374, true);
        TestUtils.sleep();
        TestUtils.clickAtCoordinates(device, map, 8.3896274, 47.3902424, true);
        TestUtils.sleep();
        TestUtils.clickAtCoordinates(device, map, 8.3897000, 47.3903500, true);
        TestUtils.sleep();
        // undo last addition
        final String undoString = context.getString(R.string.undo);
        assertTrue(TestUtils.clickMenuButton(device, undoString, false, false));
        TestUtils.sleep();
        assertTrue(TestUtils.clickMenuButton(device, undoString, false, false));
        TestUtils.sleep();
        assertTrue(TestUtils.clickMenuButton(device, undoString, false, false));
        TestUtils.sleep();
        assertTrue(TestUtils.clickMenuButton(device, undoString, false, false));
        TestUtils.textGone(device, addWayString, 500);

        Way way = App.getLogic().getSelectedWay();
        assertNull(way);
        // nothing to undo
        assertFalse(App.getLogic().getUndo().canUndo());
        // nothing to upload
        
 App.getDelegator().getApiStorage().logStorage();
        assertEquals(prevChanges, App.getDelegator().getApiElementCount());
    }

    /**
     * Create a new Note
     */
    // @SdkSuppress(minSdkVersion = 26)
    @Test
    public void newBug() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        Resources r = context.getResources();
        String notesSelector = r.getString(R.string.bugfilter_notes);
        Set<String> set = new HashSet<>(Arrays.asList(notesSelector));
        p.edit().putStringSet(r.getString(R.string.config_bugFilter_key), set).commit();
        if (map.getTaskLayer() == null) {
            de.blau.android.layer.Util.addLayer(main, LayerType.TASKS);
            main.getMap().setPrefs(context, prefs);
            map.invalidate();
        }
        map.getDataLayer().setVisible(true);
        de.blau.android.layer.tasks.MapOverlay taskLayer = map.getTaskLayer();
        assertNotNull(taskLayer);
        taskLayer.setVisible(true);
        TestUtils.zoomToLevel(device, main, 21);
        TestUtils.unlock(device);
        TestUtils.clickSimpleButton(device);
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.menu_add_map_note), true, false));
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.simple_add_note)));
        TestUtils.clickAtCoordinates(device, map, 8.3890736, 47.3896628, true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.openstreetbug_new_title)));
        UiObject editText = device.findObject(new UiSelector().clickable(true).resourceId(device.getCurrentPackageName() + ":id/openstreetbug_comment"));
        try {
            editText.click(); // NOTE this seems to be necessary
            editText.setText("test");
        } catch (UiObjectNotFoundException e) {
            fail(e.getMessage());
        }
        assertTrue(TestUtils.clickText(device, true, context.getString(R.string.Save), true, false));
        List<Task> tasks = App.getTaskStorage().getTasks();
        assertEquals(1, tasks.size());
        Task t = tasks.get(0);
        assertTrue(t instanceof Note);
        assertEquals("test", ((Note) t).getLastComment().getText());
        TestUtils.sleep();
        // new note mode
        TestUtils.clickAtCoordinates(device, map, 8.3890736, 47.3896628, true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_newnoteselect)));
        TestUtils.drag(device, map,8.3890736, 47.3896628, 8.3893, 47.3899, true, 20);
        
        TestUtils.clickAtCoordinates(device, map, 8.3893, 47.3899, true);
        assertTrue(TestUtils.findText(device, false, "test"));
        assertTrue(TestUtils.clickText(device, true, context.getString(R.string.cancel), true, false));
    }
}
