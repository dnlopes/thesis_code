package tests;


import database.util.ExecutionPolicy;
import database.util.PrimaryKey;
import database.util.PrimaryKeyValue;
import runtime.operation.OperationTransformer;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by dnlopes on 09/05/15.
 */
public class OpsTransformationTest
{
	public static void main(String args[])
	{


		PrimaryKey pk = new PrimaryKey("a", 1);
		List<PrimaryKeyValue> pkValues = new LinkedList<>();
		for(int i = 0; i < 5; i++)
		{
			//PrimaryKeyValue value = new PrimaryKeyValue(String.valueOf(i), "t0");
			//pkValues.add(value);
		}

		ExecutionPolicy deletePolicy = ExecutionPolicy.DELETEWINS;
		ExecutionPolicy updatePolicy = ExecutionPolicy.UPDATEWINS;



		String transformed = OperationTransformer.buildDeleteOperation("t0", deletePolicy, pk, pkValues);
		transformed = OperationTransformer.buildDeleteOperation("t0", updatePolicy, pk, pkValues);



	}

}
