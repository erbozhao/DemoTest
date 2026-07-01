package com.onus.demotest.threadpool;

import java.util.Comparator;

/**
 * Description
 *
 * @author xiandongluo
 * @see
 * @since 2020/10/10
 */
public class PriorityComparator implements Comparator
{

	public static final PriorityComparator DEFAULT = new PriorityComparator();

	@Override
	public int compare(Object o1, Object o2)
	{
		if (o1 == null || o1 == null)
		{
			return 0;
		}

		Object o1Obj = o1;
		if (o1 instanceof CVFutureTask)
		{
			o1Obj = ((CVFutureTask) o1).task;
		}

		Object o2Obj = o2;
		if (o2 instanceof CVFutureTask)
		{
			o2Obj = ((CVFutureTask) o2).task;
		}


		PriorityComparable leftObj = null;
		PriorityComparable rightObj = null;
		if (o1Obj instanceof PriorityComparable)
		{
			leftObj = (PriorityComparable) o1Obj;
		}


		if (o2Obj instanceof PriorityComparable)
		{
			rightObj = (PriorityComparable) o2Obj;
		}
		PriorityComparable.Priority leftPriority = (leftObj == null ? PriorityComparable.Priority.NORMAL : leftObj.priority());
		PriorityComparable.Priority rightPriority = (rightObj == null ? PriorityComparable.Priority.NORMAL : rightObj.priority());

		if (leftPriority == rightPriority)
		{
			int leftSequence = leftObj == null ? 0 : leftObj.sequence();
			int rightSequence = rightObj == null ? 0 : rightObj.sequence();
			return leftSequence - rightSequence;
		}
		else
		{
			return rightPriority.ordinal() - leftPriority.ordinal();
		}
	}
}
