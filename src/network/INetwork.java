package network;

import runtime.operation.Operation;


/**
 * Created by dnlopes on 15/03/15.
 */
public interface INetwork
{

	public void sendOperation(Operation op);
	public void sendToNode(Operation op, Node node);
	public void sendBytes(byte[] message, Node to);
	public void addNode(Node n);
}
