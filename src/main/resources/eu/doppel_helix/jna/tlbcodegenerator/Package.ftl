[#ftl]

/**
 * ${name}
[#if (docString)?has_content] *
 * <p>${docString!}</p>
 *
[/#if] * <p>uuid(${guid})</p>
 * <p>version(${majorversion?c}.${minorversion?c})</p>
 */
package ${package};