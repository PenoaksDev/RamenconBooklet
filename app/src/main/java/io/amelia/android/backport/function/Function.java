package io.amelia.android.backport.function;

import io.amelia.android.support.Objs;

/**
 * Represents a function that accepts one argument and produces a result.
 * <p>
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object)}.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
public interface Function<T, R>
{
	/**
	 * Returns a function that always returns its input argument.
	 *
	 * @param <T> the type of the input and output objects to the function
	 * @return a function that always returns its input argument
	 */
	static <T> Function<T, T> identity()
	{
		return new Function<T, T>()
		{
			@Override
			public T apply( T t )
			{
				return t;
			}
		};
	}

	/**
	 * Returns a composed function that first applies this function to
	 * its input, and then applies the {@code after} function to the result.
	 * If evaluation of either function throws an exception, it is relayed to
	 * the caller of the composed function.
	 *
	 * @param <V>   the type of output of the {@code after} function, and of the
	 *              composed function
	 * @param after the function to apply after this function is applied
	 * @return a composed function that first applies this function and then
	 * applies the {@code after} function
	 * @throws NullPointerException if after is null
	 * @see #compose(Function)
	 */
	default <V> Function<T, V> andThen( Function<? super R, ? extends V> after )
	{
		Objs.notNull( after );

		return new Function<T, V>()
		{
			@Override
			public V apply( T t )
			{
				return after.apply( ( R ) apply( t ) );
			}
		};
	}

	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function argument
	 * @return the function result
	 */
	R apply( T t );

	/**
	 * Returns a composed function that first applies the {@code before}
	 * function to its input, and then applies this function to the result.
	 * If evaluation of either function throws an exception, it is relayed to
	 * the caller of the composed function.
	 *
	 * @param <V>    the type of input to the {@code before} function, and to the
	 *               composed function
	 * @param before the function to apply before this function is applied
	 * @return a composed function that first applies the {@code before}
	 * function and then applies this function
	 * @throws NullPointerException if before is null
	 * @see #andThen(Function)
	 */
	default <V> Function<V, R> compose( Function<? super V, ? extends T> before )
	{
		Objs.notNull( before );

		return new Function<V, R>()
		{
			@Override
			public R apply( V v )
			{
				return apply( ( V ) before.apply( v ) );
			}
		};
	}
}