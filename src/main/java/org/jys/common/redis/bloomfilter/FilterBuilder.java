package org.jys.common.redis.bloomfilter;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * @author YueSong Jiang
 * @date 2019/3/15
 * @description <p> </p>
 */
public class FilterBuilder implements Cloneable, Serializable {
    private static final long serialVersionUID = 1;
    private boolean overwriteIfExists = false;
    private Integer expectedElements;
    private Integer size;
    private Integer hashes;
    private Integer countingBits = 16;
    private Double falsePositiveProbability;
    private String name = "";
    private HashProvider.HashMethod hashMethod = HashProvider.HashMethod.Murmur3KirschMitzenmacher;
    private HashProvider.HashFunction hashFunction = HashProvider.HashMethod.Murmur3KirschMitzenmacher.getHashFunction();

    private static transient Charset defaultCharset = Charset.forName("UTF-8");
    private boolean done = false;

    private long gracePeriod = TimeUnit.HOURS.toMillis(6);
    private long cleanupInterval = TimeUnit.HOURS.toMillis(1);

    private transient StringRedisTemplate template;

    /**
     * Constructs a new builder for Bloom filters and counting Bloom filters.
     */
    public FilterBuilder() {
    }

    /**
     * Constructs a new Bloom Filter Builder by specifying the expected size of the filter and the tolerable false
     * positive probability. The size of the BLoom filter in in bits and the optimal number of hash functions will be
     * inferred from this.
     *
     * @param expectedElements         expected elements in the filter
     * @param falsePositiveProbability tolerable false positive probability
     */
    public FilterBuilder(int expectedElements, double falsePositiveProbability) {
        this.expectedElements(expectedElements).falsePositiveProbability(falsePositiveProbability);
    }

    /**
     * Constructs a new Bloom Filter Builder using the specified size in bits and the specified number of hash
     * functions.
     *
     * @param size   bit size of the Bloom filter
     * @param hashes number of hash functions to use
     */
    public FilterBuilder(int size, int hashes) {
        this.size(size).hashes(hashes);
    }

    /**
     * Sets the number of expected elements. In combination with the tolerable false positive probability, this is used
     * to infer the optimal size and optimal number of hash functions of the filter.
     *
     * @param expectedElements number of expected elements.
     * @return the modified FilterBuilder (fluent interface)
     */
    public FilterBuilder expectedElements(int expectedElements) {
        this.expectedElements = expectedElements;
        return this;
    }

    /**
     * Sets the size of the filter in bits.
     *
     * @param size size of the filter in bits
     * @return the modified FilterBuilder (fluent interface)
     */
    public FilterBuilder size(int size) {
        this.size = size;
        return this;
    }

    /**
     * Sets the tolerable false positive probability. In combination with the number of expected elements, this is used
     * to infer the optimal size and optimal number of hash functions of the filter.
     *
     * @param falsePositiveProbability the tolerable false
     * @return the modified FilterBuilder (fluent interface)
     */
    public FilterBuilder falsePositiveProbability(double falsePositiveProbability) {
        this.falsePositiveProbability = falsePositiveProbability;
        return this;
    }

    /**
     * Set the number of hash functions to be used.
     *
     * @param numberOfHashes number of hash functions used by the filter.
     * @return the modified FilterBuilder (fluent interface)
     */
    public FilterBuilder hashes(int numberOfHashes) {
        this.hashes = numberOfHashes;
        return this;
    }

    /**
     * Sets the number of bits used for counting in case of a counting Bloom filter. For non-counting Bloom filters this
     * setting has no effect. <p><b>Default</b>: 16</p>
     *
     * @param countingBits Number of counting bits used by the counting Bloom filter
     * @return the modified FilterBuilder (fluent interface)
     */
    public FilterBuilder countingBits(int countingBits) {
        this.countingBits = countingBits;
        return this;
    }

    /**
     * Sets the name of the Bloom filter. If a redis-backed Bloom filter with the provided name exists and it is
     * compatible to this FilterBuilder configuration, it will be loaded and used. This behaviour can be changed by
     * {@link #overwriteIfExists(boolean)}. <p><b>Default</b>: ""</p>
     *
     * @param name The name of the filter
     * @return the modified FilterBuilder (fluent interface)
     */
    public FilterBuilder name(String name) {
        this.name = name;
        return this;
    }

    public FilterBuilder template(StringRedisTemplate template) {
        this.template = template;
        return this;
    }

    /**
     * Sets whether any existing Bloom filter with same name should be overwritten in Redis. <p><b>Default</b>:
     * <tt>false</tt></p>
     *
     * @param overwrite boolean indicating whether to overwrite any existing filter with the same name
     * @return the modified FilterBuilder (fluent interface)
     */
    public FilterBuilder overwriteIfExists(boolean overwrite) {
        this.overwriteIfExists = overwrite;
        return this;
    }


    /**
     * Sets the method used to generate hash values. Possible hash methods are documented in the corresponding enum
     * {@link HashProvider.HashMethod}. <p><b>Default</b>: MD5</p>
     * <p>
     * For the generation of hash values the String representation of objects is used.
     *
     * @param hashMethod the method used to generate hash values
     * @return the modified FilterBuilder (fluent interface)
     */
    public FilterBuilder hashFunction(HashProvider.HashMethod hashMethod) {
        this.hashMethod = hashMethod;
        this.hashFunction = hashMethod.getHashFunction();
        return this;
    }

    /**
     * Uses a given custom hash function.
     *
     * @param hf the custom hash function
     * @return the modified FilterBuilder (fluent interface)
     */
    public FilterBuilder hashFunction(HashProvider.HashFunction hf) {
        this.hashFunction = hf;
        return this;
    }

    /**
     * Sets the grace period in milliseconds.
     *
     * @param gracePeriodInMillis The grace period to set, in milliseconds.
     * @return the modified FilterBuilder (fluent interface)
     */
    public FilterBuilder gracePeriod(long gracePeriodInMillis) {
        this.gracePeriod = gracePeriodInMillis;
        return this;
    }

    /**
     * Sets the grace period.
     *
     * @param gracePeriod The grace period to set, in the provided time unit.
     * @param unit        The time unit in which the grace period is given.
     * @return the modified FilterBuilder (fluent interface)
     */
    public FilterBuilder gracePeriod(long gracePeriod, TimeUnit unit) {
        this.gracePeriod = unit.toMillis(gracePeriod);
        return this;
    }

    /**
     * Gets the grace period in milliseconds.
     *
     * @return the grace period
     */
    public long gracePeriod() {
        return this.gracePeriod;
    }

    /**
     * Gets the grace period in the provided time unit.
     *
     * @param unit The {@link TimeUnit} to which the Grace Period is converted
     * @return the grace period in the provided time unit
     */
    public long gracePeriod(TimeUnit unit) {
        return unit.convert(this.gracePeriod, TimeUnit.MILLISECONDS);
    }

    /**
     * Sets the cleanup interval in milliseconds.
     *
     * @param cleanupIntervalInMillis The cleanup interval to set, in milliseconds.
     * @return the modified FilterBuilder (fluent interface)
     */
    public FilterBuilder cleanupInterval(long cleanupIntervalInMillis) {
        this.cleanupInterval = cleanupIntervalInMillis;
        return this;
    }

    /**
     * Sets the cleanup interval.
     *
     * @param cleanupInterval The cleanup interval to set, in the provided time unit.
     * @param unit            The time unit in which the cleanup interval is given.
     * @return the modified FilterBuilder (fluent interface)
     */
    public FilterBuilder cleanupInterval(long cleanupInterval, TimeUnit unit) {
        this.cleanupInterval = unit.toMillis(cleanupInterval);
        return this;
    }

    /**
     * Gets the cleanup interval in milliseconds.
     *
     * @return the cleanup interval
     */
    public long cleanupInterval() {
        return this.cleanupInterval;
    }

    /**
     * Gets the cleanup interval in the provided time unit.
     *
     * @param unit The {@link TimeUnit} to which the cleanup interval is converted
     * @return the cleanup interval in the provided time unit
     */
    public long cleanupInterval(TimeUnit unit) {
        return unit.convert(this.cleanupInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * Checks if all necessary parameters were set and tries to infer optimal parameters (e.g. size and hashes from
     * given expectedElements and falsePositiveProbability). This is done automatically.
     *
     * @return the completed FilterBuilder
     */
    public FilterBuilder complete() {
        if (done) {
            return this;
        }
        if (size == null && expectedElements != null && falsePositiveProbability != null) {
            size = optimalM(expectedElements, falsePositiveProbability);
        }
        if (hashes == null && expectedElements != null && size != null) {
            hashes = optimalK(expectedElements, size);
        }
        if (size == null || hashes == null) {
            throw new NullPointerException("Neither (expectedElements, falsePositiveProbability) nor (size, hashes) were specified.");
        }
        if (expectedElements == null) {
            expectedElements = optimalN(hashes, size);
        }
        if (falsePositiveProbability == null) {
            falsePositiveProbability = optimalP(hashes, size, expectedElements);
        }

        done = true;
        return this;
    }


    @Override
    public FilterBuilder clone() {
        Object clone;
        try {
            clone = super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Cloning failed.");
        }
        return (FilterBuilder) clone;
    }


    /**
     * @return the number of expected elements for the Bloom filter
     */
    public int expectedElements() {
        return expectedElements;
    }

    /**
     * @return the size of the Bloom filter in bits
     */
    public int size() {
        return size;
    }

    /**
     * @return the number of hashes used by the Bloom filter
     */
    public int hashes() {
        return hashes;
    }

    /**
     * @return The number of bits used for counting in case of a counting Bloom filter
     */
    public int countingBits() {
        return countingBits;
    }

    /**
     * @return the tolerable false positive probability of the Bloom filter
     */
    public double falsePositiveProbability() {
        return falsePositiveProbability;
    }

    /**
     * @return the name of the Bloom filter
     */
    public String name() {
        return name;
    }

    public StringRedisTemplate template() {
        return template;
    }

    /**
     * @return The hash method to be used by the Bloom filter
     */
    public HashProvider.HashMethod hashMethod() {
        return hashMethod;
    }

    /**
     * @return the actual hash function to be used by the Bloom filter
     */
    public HashProvider.HashFunction hashFunction() {
        return hashFunction;
    }

    /**
     * @return Return the default Charset used for conversion of String values into byte arrays used for hashing
     */
    public static Charset defaultCharset() {
        return defaultCharset;
    }

    /**
     * @return {@code true} if the Bloom filter that is to be built should overwrite any existing Bloom filter with the
     * same name
     */
    public boolean overwriteIfExists() {
        return overwriteIfExists;
    }


    /**
     * Checks whether a configuration is compatible to another configuration based on the size of the Bloom filter and
     * its hash functions.
     *
     * @param other the other configuration
     * @return {@code true} if the configurations are compatible
     */
    public boolean isCompatibleTo(FilterBuilder other) {
        return this.size() == other.size() && this.hashes() == other.hashes() && this.hashMethod() == other.hashMethod();
    }

    /**
     * Calculates the optimal size <i>size</i> of the bloom filter in bits given <i>expectedElements</i> (expected
     * number of elements in bloom filter) and <i>falsePositiveProbability</i> (tolerable false positive rate).
     *
     * @param n Expected number of elements inserted in the bloom filter
     * @param p Tolerable false positive rate
     * @return the optimal size <i>size</i> of the bloom filter in bits
     */
    public static int optimalM(long n, double p) {
        return (int) Math.ceil(-1 * (n * Math.log(p)) / Math.pow(Math.log(2), 2));
    }

    /**
     * Calculates the optimal <i>hashes</i> (number of hash function) given <i>expectedElements</i> (expected number of
     * elements in bloom filter) and <i>size</i> (size of bloom filter in bits).
     *
     * @param n Expected number of elements inserted in the bloom filter
     * @param m The size of the bloom filter in bits.
     * @return the optimal amount of hash functions hashes
     */
    public static int optimalK(long n, long m) {
        return (int) Math.ceil((Math.log(2) * m) / n);
    }

    /**
     * Calculates the amount of elements a Bloom filter for which the given configuration of size and hashes is
     * optimal.
     *
     * @param k number of hashes
     * @param m The size of the bloom filter in bits.
     * @return amount of elements a Bloom filter for which the given configuration of size and hashes is optimal.
     */
    public static int optimalN(long k, long m) {
        return (int) Math.ceil((Math.log(2) * m) / k);
    }

    /**
     * Calculates the best-case (uniform hash function) false positive probability.
     *
     * @param k                number of hashes
     * @param m                The size of the bloom filter in bits.
     * @param insertedElements number of elements inserted in the filter
     * @return The calculated false positive probability
     */
    public static double optimalP(long k, long m, double insertedElements) {
        return Math.pow((1 - Math.exp(-k * insertedElements / (double) m)), k);
    }
}
