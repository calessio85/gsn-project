package gsn.vsensor;

import gsn.beans.DataTypes;
import gsn.beans.StreamElement;
import gsn.beans.VSensorConfig;
import gsn.others.visualization.svg.SVGCircle;
import gsn.others.visualization.svg.SVGEdge;
import gsn.others.visualization.svg.SVGLayer;
import gsn.others.visualization.svg.SVGPage;
import gsn.others.visualization.svg.SVGUtils;
import gsn.utils.LazyTimedHashMap;
import gsn.utils.ParamParser;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.naming.OperationNotSupportedException;

import org.apache.log4j.Logger;

public class WebInteractiveVirtualSensor extends AbstractVirtualSensor {
   
   private static final transient Logger logger            = Logger.getLogger( WebInteractiveVirtualSensor.class );
   
   /*
    * This method is going to be called by the container when one of the input
    * streams has a data to be delivered to this virtual sensor. After receiving
    * the data, the virutal sensor can do the processing on it and this
    * processing could possibly result in producing a new stream element in this
    * virtual sensor in which case the virutal sensor will notify the container
    * by simply adding itself to the list of the virtual sensors which have
    * produced data. (calling <code>container.publishData(this)</code>. For
    * more information please check the <code>AbstractVirtalSensor</code>
    * @param inputStreamName is the name of the input stream as specified in the
    * configuration file of the virtual sensor. @param inputDataFromInputStream
    * is actually the real data which is produced by the input stream and should
    * be delivered to the virtual sensor for possible processing.
    */
   private LazyTimedHashMap              lazyTimedHashMap;
   
   private final String                  INPUT_STREAM_NAME = "DATA";
   
   private final String                  OUTPUT_FIELD_NAME = "PLOT";
   
   private int                           counter_pref      = 0;
   
   private VSensorConfig                 vsensor;
   
   public boolean initialize ( ) {
      TreeMap < String , String > params = getVirtualSensorConfiguration( ).getMainClassInitialParams( );
      int memorySizeInSeconds = ParamParser.getInteger( params.get( "memory-size-in-seconds" ) , -1 );
      if ( memorySizeInSeconds == -1 ) {
         logger.error( "The parameter *memory-size-in-seconds* is missing from the virtual sensor processing class's initialization." );
         logger.error( "Loading the virtual sensor failed" );
         return false;
      } else {
         lazyTimedHashMap = new LazyTimedHashMap( memorySizeInSeconds * 1000 );
      }
      return true;
   }
   
   public void dataAvailable ( String inputStreamName , StreamElement streamElement ) {
      if ( inputStreamName.equalsIgnoreCase( INPUT_STREAM_NAME ) ) {
         int node_id = ( Integer ) streamElement.getData( "NODE_ID" );
         int parent_id = ( Integer ) streamElement.getData( "PARENT_ID" );
         float tempreature = 0f;
         SVGCircle simpleNodeObject = new SVGCircle( new SimpleNodeObject( node_id , parent_id , SimpleNodeObject.REQUEST_TYPE_DATA , tempreature ) , Color.blue );
         lazyTimedHashMap.put( node_id , simpleNodeObject );
      }
      ArrayList < SVGCircle > arrayList = lazyTimedHashMap.getValues( );
      byte [ ] visualizedResults = visualaize( 400 , 400 , arrayList ).getBytes( );
      StreamElement out = new StreamElement( new String [ ] { OUTPUT_FIELD_NAME } , new Byte[ ] { DataTypes.BINARY } , new Serializable [ ] { visualizedResults } , System.currentTimeMillis( ) );
      dataProduced( out );
      
   }
   
   public boolean dataFromWeb ( String command, String[] paramNames, Serializable[] paramValues ) {
      String streamSourceAliasName = "ss_bla";
      try {
        return  vsensor.getInputStream( INPUT_STREAM_NAME ).getSource( streamSourceAliasName ).getWrapper().sendToWrapper( command , paramNames , paramValues );
      } catch ( OperationNotSupportedException e ) {
         logger.warn( new StringBuilder( ).append( "The virtual sensor : " ).append( vsensor.getName( ) ).append(
            " want to send data to a stream source which doesn't support receiving data." ).toString( ) );
         logger.warn( e.getMessage( ) , e );
         return false;
      }
   }
   
   public static String visualaize ( int width , int height , ArrayList < SVGCircle > nodes ) {
      ArrayList < SVGEdge > edges = new ArrayList < SVGEdge >( );
      SVGLayer edgesLayer = new SVGLayer( "Edges" , 0.8f );
      SVGLayer nodesLayer = new SVGLayer( "Nodes" , 1f );
      for ( SVGCircle node : nodes )
         for ( SVGCircle parent : nodes )
            if ( node != parent && ( ( SimpleNodeObject ) node.getObject( ) ).getParentID( ) == ( ( SimpleNodeObject ) parent.getObject( ) ).getNodeID( ) ) {
               SVGEdge edge = new SVGEdge( node , parent , false );
               edgesLayer.addElement( edge );
               edges.add( edge );
               edge.setWidth( 3 );
            }
      nodesLayer.addElements( nodes );
      SVGPage svgPage = new SVGPage( width , height );
      svgPage.addLayer( edgesLayer ).addLayer( nodesLayer );
      
      SVGUtils.performLayout( nodes , edges , SVGUtils.TREE_HORIZ , svgPage.getWidth( ) , svgPage.getHeight( ) );
      StringBuilder toReturn = new StringBuilder( );
      svgPage.drawOn( toReturn );
      return toReturn.toString( );
   }
   
   public void dispose ( ) {

   }
   
}

class SimpleNodeObject {
   
   private final int       NOT_SPECIFIED     = -1;
   
   public static final int REQUEST_TYPE_DATA = 1;
   
   int                     nodeID            = NOT_SPECIFIED;
   
   int                     parentID          = NOT_SPECIFIED;
   
   int                     requestType       = NOT_SPECIFIED;
   
   double                  tempreatureValue  = NOT_SPECIFIED;
   
   public SimpleNodeObject ( int nodeID , int parentID , int requestType , double tempreatureValue ) {
      this.nodeID = nodeID;
      this.parentID = parentID;
      this.requestType = requestType;
      this.tempreatureValue = tempreatureValue;
   }
   
   public int getNodeID ( ) {
      return nodeID;
   }
   
   public int getParentID ( ) {
      return parentID;
   }
   
   public int getRequestType ( ) {
      return requestType;
   }
   
   public double getTempreatureValue ( ) {
      return tempreatureValue;
   }
   
}
