/*
 * The MIT License
 *
 * Copyright 2011 ASementsov.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jsf2jpa.jsf.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.jsf2jps.utils.NamingConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class implements userConfig functions
 *
 * <br/>$LastChangedRevision:$
 * <br/>$LastChangedDate:$
 *
 * @author ASementsov
 */
@ManagedBean(name=UserConfig.BEAN_NAME)
@SessionScoped
public class UserConfig implements Serializable
{
    /**
     * Subversion revision number it will be changed automatically when commited
     */
    private static final String REV_NUMBER = "$Revision:$";
    public static final String BEAN_NAME = "userConfig";
    
    private static final String CONFIG_BASE = "/config/";
    private static final String CONFIG_FILE = "config";
    private static final String TABLE_TAG = "Table";
    private static final String COLUMN_TAG = "column";
    private static final String FILE_EXT = ".xml";
    private static final String USER_ATTR_COLUMNS = "attrColumns";

    private Map<String, TableConfig>    loadedTables = new HashMap<String, TableConfig>();

    private final class TableXMLParser extends DefaultHandler
    {
        private TableConfig cfg;
        
        public TableConfig parse (InputStream is)
        {
            try {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser sp = spf.newSAXParser();
                sp.parse(is, this);
                return cfg;
            }
            catch (Exception e) {
                throw (new RuntimeException (e));
            }
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
        {
            if (qName.equals(TABLE_TAG)) {
                cfg = new TableConfig();
                cfg.setTableName(attributes.getValue(NamingConstants.NAME));
                cfg.setVar(attributes.getValue(NamingConstants.VAR));
            }
            else if (qName.equals(COLUMN_TAG)) {
                ColumnConfig col = new ColumnConfig();
                for (String field : ColumnConfig.getAccessors().keySet()) {
                    try {
                        if (ColumnConfig.getAccessors().get(field).getSet() != null) {
                            ColumnConfig.getAccessors().get(field).getSet().invoke(col, attributes.getValue(field));
                        }
                    }
                    catch (Exception ex) {
                        Logger.getLogger(UserConfig.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                cfg.getColumns().add(col);
            }
        }
    }

    private final class ConfigXMLParser extends DefaultHandler implements Serializable
    {
        private String                  userDirectory;
        private static final String     CONFIG_TAG = "config";
        private static final String     USER_DIR_ATTR = "config-directory";

        public void parse (InputStream is)
        {
            try {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser sp = spf.newSAXParser();
                sp.parse(is, this);
            }
            catch (Exception e) {
                throw (new RuntimeException (e));
            }
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
        {
            if (qName.equals(CONFIG_TAG)) {
                userDirectory = attributes.getValue(USER_DIR_ATTR);
                defAttrColumns = Integer.valueOf (attributes.getValue(USER_ATTR_COLUMNS));
            }
        }
    }

    private ConfigXMLParser             configuration;
    private int                         defAttrColumns = 2;
    
    private void loadConfiguration(String configName)
    {
        String url = CONFIG_BASE + configName + FILE_EXT;
        InputStream is = getClass().getResourceAsStream(url);
        configuration = new ConfigXMLParser();
        configuration.parse(is);

        try {
            is.close();
        }
        catch (IOException ex) {
            Logger.getLogger(UserConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    /**
     * Function load table from resource file
     * @param tableName - table name
     * @return tabble config object
     */
    public TableConfig loadTable(String tableName)
    {        
        String url = CONFIG_BASE + tableName + FILE_EXT;
        InputStream is = getClass().getResourceAsStream(url);
        /*
         * Not found in resources try to find it in the uiser directory
         */
        if (is == null) {
            try {
                is = new FileInputStream(configuration.userDirectory + File.separatorChar + tableName + FILE_EXT);
            }
            catch (Exception e) {
                 Logger.getLogger(UserConfig.class.getName()).log(
                    Level.WARNING, 
                    "Error while trying to loadTable table from XML {0}", 
                    e);
            }
        }
        
        TableXMLParser p = new TableXMLParser();
        TableConfig tbl = p.parse(is);
        if (tbl != null) 
            loadedTables.put(tableName, tbl);
        
        try {
            is.close();
        }
        catch (IOException ex) {
            Logger.getLogger(UserConfig.class.getName()).log(Level.SEVERE, null, ex);
        }

        return tbl;
    }

    /**
     * Fucntion stores table configuration in the xml file
     * @param userName - userName
     * @param table - table name
     */
    public void storeTable (String userName, TableConfig table)
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document dom = null;
        
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.newDocument();
            Element tableEl = createTableElement (dom, table);
            dom.appendChild(tableEl);
            
            /*
             * Store table in the resource file
             */
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS domImplLS = (DOMImplementationLS)registry.getDOMImplementation("LS");

            LSSerializer ser = domImplLS.createLSSerializer();
            LSOutput out = domImplLS.createLSOutput();

            /*
             * Store XML to file
             */
            if (configuration == null)
                loadConfiguration(CONFIG_FILE);
            
            String url = configuration.userDirectory + table.getTableName() + FILE_EXT;
            
            /*
             * Create directories if its does not exists
             */
            File fp = new File(configuration.userDirectory);
            if (!fp.exists())
                fp.mkdirs();

            FileOutputStream fos = new FileOutputStream(url);
            out.setByteStream(fos);
            ser.write(dom, out);
            fos.close();
        }
        catch(Exception pce) {
            Logger.getLogger(UserConfig.class.getName()).log(
                    Level.WARNING, 
                    "Error while trying to store table in XML {0}", 
                    pce);
        }
    }
    
    /**
     * Fucntion create DOM element for the table config object
     * @param dom - DOM Document object
     * @param table - table cofig object
     * @return newly created table element
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException 
     */
    private Element createTableElement(Document dom, TableConfig table) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Element tableEle = dom.createElement(TABLE_TAG);
        tableEle.setAttribute(NamingConstants.NAME, table.getTableName());
        tableEle.setAttribute(NamingConstants.VAR, table.getVar());
        
        for (ColumnConfig col : table.getColumns()) {
            Element colEle = dom.createElement(COLUMN_TAG);
            for (Map.Entry<String, PropertyAccessor> entr : ColumnConfig.getAccessors().entrySet()) {
                if (entr.getValue().getGet() != null)
                    colEle.setAttribute(entr.getKey(), (String)entr.getValue().getGet().invoke(col));
            }

            tableEle.appendChild(colEle);
        }

        return tableEle;
    }

    public TableConfig getTable(String tableName)
    {
        if (configuration == null) {
            loadConfiguration(CONFIG_FILE);
        }

        if (loadedTables.containsKey(tableName))
            return loadedTables.get(tableName);
        else
            return loadTable(tableName);
    }

    public int getDefAttrColumns()
    {
        return defAttrColumns;
    }

    public void setDefAttrColumns(int defAttrColumns)
    {
        this.defAttrColumns = defAttrColumns;
    }
}
