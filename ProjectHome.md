# Application for testing primefaces #

---

## Description ##

  * Testing for dynamically created components
  * Testing 

&lt;p:dataTable&gt;

**component with JPA 2.0 + EJB 3.1 engine.
    1. Configuration classes to manipulate table configuration data stored in XML file
    1. Managed bean which can read/store table configuration data from/to XML file. This bean is session scoped
    1. Managed bean which contains data used by tested JSF page
    1. JSF 2.0 page which use to test primefaces library
  * Testing**

&lt;p:tree&gt;

**component
    * Dynamically loaded tree nodes from JPA through the EJB
  * Testing**

&lt;p:layout&gt;

**component
  * Testing LazyTableModel with CriteriaAPI
    * In**

&lt;p:dataTable&gt;

 filter fields:
      * for numeric values allowed '<' '>' and '=' operands
      * for string values '%'(like) operand
    * allowed filter by joined tables (only one join level)
    * allowed filter by attribute's values
    * sorting allowed only for basic fields

---

## Getting started ##
> > ### 1. Checkout sources ###
      * Run Netbeans 6.9 or 7.0
      * Import projects:
        1. jsf2jps-ejb
        1. jsf2jps-web
        1. jsf2jps-ear
      * Change jsf2jpa-ejb\src\main\resources\META-INF\persistence.xml to properly values
      * Build projects
      * Run jsf2jps-ear project or deploy jsf2jpa-ear.ear to Glassfish v3.x server
      * See http://localhost:8080/jsf2jpa-web/index.xhtml and http://localhost:8080/jsf2jpa-web/hier.xhtml pages
> > ### 2. Download jsf2jpa-ear.ear ###
      * deploy jsf2jpa-ear.ear to Glassfish v3.x server
      * See http://localhost:8080/jsf2jpa-web/index.xhtml and http://localhost:8080/jsf2jpa-web/hier.xhtml pages