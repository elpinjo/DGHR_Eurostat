package dghr.eurostat.services;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
// --- <<IS-END-IMPORTS>> ---

public final class common

{
	// ---( internal utility methods )---

	final static common _instance = new common();

	static common _newInstance() { return new common(); }

	static common _cast(Object o) { return (common)o; }

	// ---( server methods )---




	public static final void getLabelIndexes (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getLabelIndexes)>> ---
		// @sigtype java 3.5
		// [i] field:0:required position
		// [i] recref:1:required dimension dghr.eurostat.datamodels:dimension
		// [o] field:1:required indexes
		IDataCursor pipelineCursor = pipeline.getCursor();
		String positionStr = IDataUtil.getString( pipelineCursor, "position" );
		int position = Integer.parseInt(positionStr);
		IData[] dimensions = IDataUtil.getIDataArray( pipelineCursor, "dimension"  );
		int totalDims = dimensions.length;
		String[] indexes = new String[totalDims];
		int currentSize = 1;
		int startDim = totalDims-1;
		boolean firstMulti = true;
		for (int dimPos=startDim;dimPos>-1;dimPos--){ //work backwards through the dimensions
			IDataCursor dimCursor = dimensions[dimPos].getCursor();
			int dimSize = IDataUtil.getInt( dimCursor, "size", 0 );
			if (dimSize == 1){
				indexes[dimPos]="0"; //nothing to switch for this dimension, just one choice
			} else {
				if (firstMulti == true) { //this is the first dimension with more than one option, use remainders
					firstMulti=false;
					long tmpLongDiv = position/dimSize;
					int tmpIntDiv = position/dimSize;
					int tmpIntRemain = position%dimSize;
					indexes[dimPos]=String.valueOf(position%dimSize); //remainder of the division between position and dimension size, multiplied by the dimension size
					currentSize = dimSize; //for the next round
				} else {
					long tmpLongDiv = position/currentSize;
					int tmpIntDiv = position/currentSize;
					int tmpIntRemain = position%currentSize;
					indexes[dimPos]=String.valueOf((position/currentSize)%dimSize); //whole number of division between position and dimension size
					currentSize = currentSize * dimSize; //for the next round
				}
			}
			dimCursor.destroy();
		}
		IDataUtil.put( pipelineCursor, "indexes", indexes ); 
		//IDataUtil.remove(pipelineCursor, "dimension");
		IDataUtil.remove(pipelineCursor, "position");
		
		
		//TODO: Read dimensions, do the math and export label indexes
		// --- <<IS-END>> ---

                
	}



	public static final void getMetaData (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getMetaData)>> ---
		// @sigtype java 3.5
		// [i] record:0:required document
		// [o] object:0:required totalDimensions
		// [o] object:0:required totalSize
		// [o] record:1:required dimension
		// [o] - field:0:required dimension
		// [o] - field:0:required label
		// [o] - object:0:required size
		// [o] - record:1:required index
		// [o] -- field:0:required name
		// [o] -- field:0:required label
		IDataCursor pipelineCursor = pipeline.getCursor();
		IData inDoc = IDataUtil.getIData( pipelineCursor, "document" );
		String outKey = "";
		IData outputDoc = IDataFactory.create();  
		IDataCursor outputDocCursor = outputDoc.getCursor();
		int totalSize = 1;
		if (inDoc != null)
		{
			IDataCursor inDocCursor = inDoc.getCursor();
			IData dimensions = IDataUtil.getIData( inDocCursor, "dimension" );
			if (dimensions != null){
				IDataCursor dimCursor = dimensions.getCursor();
				dimCursor.first();
				IData dimData = IDataUtil.getIData(dimCursor);
				if (dimData != null){
					int dimSize = IDataUtil.size(dimCursor);
					IDataUtil.put( pipelineCursor, "totalDimensions", dimSize );
					dimCursor.first();
					//outKey = dimCursor.getKey();
					//IDataUtil.put(outputDocCursor, "dimension", outKey);
					IData outputDims[] = new IData[dimSize];
					//IData outputDim = IDataFactory.create();
					for (int dimPos=0; dimPos<dimSize; dimPos++){
						//IDataCursor outputDimCursor = outputDim.getCursor();
						dimData = IDataUtil.getIData(dimCursor);
						outputDims[dimPos]= IDataFactory.create();
						IDataCursor outputDimCursor = outputDims[dimPos].getCursor();
						outKey = dimCursor.getKey();
						IDataUtil.put(outputDimCursor, "dimension", outKey);
						IDataCursor dimDetailCursor = dimData.getCursor();
						String label = IDataUtil.getString( dimDetailCursor, "label" );
						if (label != null) {
							IDataUtil.put(outputDimCursor, "label", label);
						}
								IData category = IDataUtil.getIData(dimDetailCursor,"category");
						if (category != null) {
							IDataCursor categoryCursor = category.getCursor();
							IData catIndex = IDataUtil.getIData(categoryCursor,"index");
							IData catLabel = IDataUtil.getIData(categoryCursor,"label");
							IDataCursor catIndexCursor = catIndex.getCursor();
							IDataCursor catLabelCursor = catLabel.getCursor();
							int indexSize = IDataUtil.size(catIndexCursor);
							IDataUtil.put(outputDimCursor, "size", indexSize);
							totalSize = totalSize * indexSize;
							catIndexCursor.first();
							catLabelCursor.first();
							//IDataUtil.put(outputDocCursor, "numberOfIndexes", indexSize);
							//IData indexData = IDataFactory.create();
							IData indexes[] = new IData[indexSize];
							for (int indexPos=0; indexPos<indexSize; indexPos++){
								indexes[indexPos]=IDataFactory.create();
								String indexKey = catIndexCursor.getKey();
								String indexLabel = IDataUtil.getString(catLabelCursor);
								IDataCursor indexDataCursor = indexes[indexPos].getCursor();
								IDataUtil.put(indexDataCursor, "name", indexKey);
								IDataUtil.put(indexDataCursor, "label", indexLabel);
								catIndexCursor.next();
								catLabelCursor.next();
								indexKey = catIndexCursor.getKey();
							}
							IDataUtil.put(outputDimCursor, "index", indexes);
							catIndexCursor.destroy();
							catLabelCursor.destroy();
						
						}
						dimDetailCursor.destroy();
						dimCursor.next();
					}
					
					
					IDataUtil.put( pipelineCursor, "totalSize", totalSize ); 
					IDataUtil.put( pipelineCursor, "dimension", outputDims ); 
				}
			}
		}
		outputDocCursor.destroy();  
		
		//	IDataUtil.put( pipelineCursor, "outputDoc", outputDoc ); 
		//pipelineCursor.last();
		//pipelineCursor.insertAfter("Key", outKey);
		// --- <<IS-END>> ---

                
	}
}

