package org.jys.example.common.redis.bloomfilter;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YueSong Jiang
 * Represents a Bloom filter and provides default methods for hashing.
 */
public interface BloomFilter<T> extends Cloneable, Serializable {

    int HUMAN_READABLE_MB = 8 * 1024 * 1024;

    /**
     * Adds the passed value to the filter.
     *
     * @param element value to add
     * @return {@code false} if the value did not previously exist in the filter. Note, that a false positive may occur,
     * thus the value may not have already been in the filter, but it hashed to a set of bits already in the filter.
     */
    boolean addRaw(byte[] element);


    /**
     * Adds the passed value to the filter.
     *
     * @param element value to add
     * @return {@code false} if the value did not previously exist in the filter. Note, that a false positive may occur,
     * thus the value may not have already been in the filter, but it hashed to a set of bits already in the filter.
     */
    default boolean add(T element) {
        return addRaw(toBytes(element));
    }


    /**
     * Performs a bulk add operation for a collection of elements.
     *
     * @param elements to add
     * @return a list of booleans indicating for each element, whether it was previously present in the filter
     */
    default List<Boolean> addAll(Collection<T> elements) {
        return elements.stream().map(this::add).collect(Collectors.toList());
    }

    /**
     * Removes all elements from the filter (i.e. resets all bits to zero).
     */
    void clear();

    /**
     * Tests whether an element is present in the filter (subject to the specified false positive rate).
     *
     * @param element to test
     * @return {@code true} if the element is contained
     */
    boolean contains(byte[] element);

    /**
     * Tests whether an element is present in the filter (subject to the specified false positive rate).
     *
     * @param element to test
     * @return {@code true} if the element is contained
     */
    default boolean contains(T element) {
        return contains(toBytes(element));
    }

    /**
     * Bulk-tests elements for existence in the filter.
     *
     * @param elements a collection of elements to test
     * @return a list of booleans indicating for each element, whether it is present in the filter
     */
    default List<Boolean> contains(Collection<T> elements) {
        return elements.stream().map(this::contains).collect(Collectors.toList());
    }

    /**
     * Bulk-tests elements for existence in the filter.
     *
     * @param elements a collection of elements to test
     * @return {@code true} if all elements are present in the filter
     */
    default boolean containsAll(Collection<T> elements) {
        return elements.stream().allMatch(this::contains);
    }

    /**
     * Returns the configuration/builder of the Bloom filter.
     *
     * @return the configuration/builder of the Bloom filter.
     */
    FilterBuilder config();


    /**
     * Return the size of the Bloom filter, i.e. the number of positions in the underlyling bit vector (called m in the
     * literature).
     *
     * @return the bit vector size
     */
    default int getSize() {
        return config().size();
    }

    /**
     * Returns the expected number of elements (called n in the literature)
     *
     * @return the expected number of elements
     */
    default int getExpectedElements() {
        return config().expectedElements();
    }

    /**
     * Returns the number of hash functions (called k in the literature)
     *
     * @return the number of hash functions
     */
    default int getHashes() {
        return config().hashes();
    }

    /**
     * get the filter name
     *
     * @return for redis, return the bit key
     */
    String getName();

    /**
     * Returns the expected false positive probability for the expected amounts of elements. This is independent of the
     * actual amount of elements in the filter. Use {@link #getFalsePositiveProbability(double)} for that purpose.
     *
     * @return the static expected false positive probability
     */
    default double getFalsePositiveProbability() {
        return config().falsePositiveProbability();
    }

    /**
     * Converts an element to the byte array representation used for hashing.
     *
     * @param element the element to convert
     * @return the elements byte array representation
     */
    default byte[] toBytes(T element) {
        return element.toString().getBytes(FilterBuilder.defaultCharset());
    }


    /**
     * Checks if two Bloom filters are compatible, i.e. have compatible parameters (hash function, size, etc.)
     *
     * @param other the other bloomfilter
     * @return <code>true</code> if this bloomfilter is compatible with the other one
     */
    default boolean compatible(BloomFilter<T> other) {
        return config().isCompatibleTo(other.config());
    }

    /**
     * Destroys the Bloom filter by deleting its contents and metadata
     */
    default void remove() {
        clear();
    }

    /**
     * Returns the k hash values for an inputs element in byte array form
     *
     * @param bytes input element
     * @return hash values
     */
    default int[] hash(byte[] bytes) {
        return config().hashFunction().hash(bytes, config().size(), config().hashes());
    }

    /**
     * Dispatches the hash function for a string value
     *
     * @param value the value to be hashed
     * @return array with <i>hashes</i> integer hash positions in the range <i>[0,size)</i>
     */
    default int[] hash(String value) {
        return hash(value.getBytes(FilterBuilder.defaultCharset()));
    }

    /**
     * Performs the union operation on two compatible bloom filters. This is achieved through a bitwise OR operation on
     * their bit vectors. This operations is lossless, i.e. no elements are lost and the bloom filter is the same that
     * would have resulted if all elements wer directly inserted in just one bloom filter.
     *
     * @param other the other bloom filter
     * @return <tt>true</tt> if this bloom filter could successfully be updated through the union with the provided
     * bloom filter
     */
    boolean union(BloomFilter<T> other);

    /**
     * Performs the intersection operation on two compatible bloom filters. This is achieved through a bitwise AND
     * operation on their bit vectors. The operations doesn't introduce any false negatives but it does raise the false
     * positive probability. The the false positive probability in the resulting Bloom filter is at most the
     * false-positive probability in one of the constituent bloom filters
     *
     * @param other the other bloom filter
     * @return <tt>true</tt> if this bloom filter could successfully be updated through the intersection with the
     * provided bloom filter
     */
    boolean intersect(BloomFilter<T> other);

    /**
     * Returns {@code true} if the Bloom filter does not contain any elements
     *
     * @return {@code true} if the Bloom filter does not contain any elements
     */
    boolean isEmpty();

    /**
     * Returns the probability of a false positive (approximated): <br> <code>(1 - e^(-hashes * insertedElements /
     * size)) ^ hashes</code>
     *
     * @param insertedElements The number of elements already inserted into the Bloomfilter
     * @return probability of a false positive after <i>expectedElements</i> {@link #addRaw(byte[])} operations
     */
    default double getFalsePositiveProbability(double insertedElements) {
        return FilterBuilder.optimalP(config().hashes(), config().size(), insertedElements);
    }

    /**
     * Returns the probability of a false positive (approximated) using an estimation of how many elements are currently in the filter
     *
     * @return probability of a false positive
     */
    default double getEstimatedFalsePositiveProbability() {
        return getFalsePositiveProbability(getEstimatedPopulation());
    }


    /**
     * Calculates the numbers of Bits per element, based on the expected number of inserted elements
     * <i>expectedElements</i>.
     *
     * @param n The number of elements already inserted into the Bloomfilter
     * @return The numbers of bits per element
     */
    default double getBitsPerElement(int n) {
        return config().size() / (double) n;
    }

    /**
     * Returns the probability that a bit is zero.
     *
     * @param n The number of elements already inserted into the Bloomfilter
     * @return probability that a certain bit is zero after <i>expectedElements</i> {@link #addRaw(byte[])} operations
     */
    default double getBitZeroProbability(int n) {
        return Math.pow(1 - (double) 1 / config().size(), config().hashes() * n);
    }

    /**
     * Estimates the current population of the Bloom filter (see: http://en.wikipedia.org/wiki/Bloom_filter#Approximating_the_number_of_items_in_a_Bloom_filter
     * )
     *
     * @return the estimated amount of elements in the filter
     */
    Double getEstimatedPopulation();


    /**
     * Prints the Bloom filter: metadata and data
     *
     * @return String representation of the Bloom filter
     */
    default String asString() {
        String ret = "Bloom Filter Parameters: \r\n" +
                "\t size = %sM" +
                "\t elements = %s" +
                "\t falsePositive = %s" +
                "\t hashes = %s";
        return String.format(ret, config().size() / HUMAN_READABLE_MB, config().expectedElements(),
                config().falsePositiveProbability(), config().hashes());
    }
}
