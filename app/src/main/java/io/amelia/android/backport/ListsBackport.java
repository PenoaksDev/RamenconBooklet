package io.amelia.android.backport;

import java.util.ConcurrentModificationException;
import java.util.List;

import io.amelia.android.backport.function.Consumer;
import io.amelia.android.support.Objs;

public class ListsBackport
{
	public static <E> void forEach( List<E> list, Consumer<? super E> action )
	{
		Objs.notNull( action );
		final int expectedModCount = list.size();
		@SuppressWarnings( "unchecked" )
		final E[] elementData = ( E[] ) list.toArray();
		final int size = list.size();
		for ( int i = 0; list.size() == expectedModCount && i < size; i++ )
		{
			action.accept( elementData[i] );
		}
		if ( list.size() != expectedModCount )
		{
			throw new ConcurrentModificationException();
		}
	}
}
