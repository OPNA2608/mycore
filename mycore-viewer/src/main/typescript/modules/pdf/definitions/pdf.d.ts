/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

// Type definitions for PDF.js
// Project: https://github.com/mozilla/pdf.js
// Definitions by: Josh Baldwin <https://github.com/jbaldwin/>
// Definitions: https://github.com/borisyankov/DefinitelyTyped

interface PDFPromise<T> {
    isResolved(): boolean;
    isRejected(): boolean;
    resolve(value:T): void;
    reject(reason:string): void;
    then(onResolve:(promise:T) => void, onReject?:(reason:string) => void): PDFPromise<T>;
}

interface PDFTreeNode {
    title: string;
    bold: boolean;
    italic: boolean;
    color: number[]; // [r,g,b]
    dest: any;
    items: PDFTreeNode[];
}

interface PDFInfo {
    PDFFormatVersion: string;
    IsAcroFormPresent: boolean;
    IsXFAPresent: boolean;
    [key: string]: any;	// return type is string, typescript chokes
}

interface PDFMetadata {
    parse(): void;
    get(name:string): string;
    has(name:string): boolean;
}

interface PDFSource {
    url?: string;
    data?: Uint8Array;
    httpHeaders?: any;
    password?: string;
}

interface PDFProgressData {
    loaded: number;
    total: number;
}

interface PDFDocumentProxy {

    /**
     * Total number of pages the PDF contains.
     **/
    numPages: number;

    /**
     * A unique ID to identify a PDF.  Not guaranteed to be unique.  [jbaldwin: haha what]
     **/
    fingerprint: string;

    /**
     * True if embedded document fonts are in use.  Will be set during rendering of the pages.
     **/
    embeddedFontsUsed(): boolean;

    /**
     * @param number The page number to get.  The first page is 1.
     * @return A promise that is resolved with a PDFPageProxy.
     **/
    getPage(number:number): PDFPromise<PDFPageProxy>;

    /**
     * TODO: return type of Promise<???>
     *  A promise that is resolved with a lookup table for mapping named destinations to reference numbers.
     **/
    getDestinations(): PDFPromise<any[]>;

    getDestination(obj: any): PDFPromise<any[]>;

    getPageIndex(refObj: any): PDFPromise<number>;

    /**
     *  A promise that is resolved with an array of all the JavaScript strings in the name tree.
     **/
    getJavaScript(): PDFPromise<string[]>;

    /**
     *  A promise that is resolved with an array that is a tree outline (if it has one) of the PDF.  @see PDFTreeNode
     **/
    getOutline(): PDFPromise<PDFTreeNode[]>;

    /**
     * A promise that is resolved with the info and metadata of the PDF.
     **/
    getMetadata(): PDFPromise<{ info: PDFInfo; metadata: PDFMetadata }>;

    /**
     * Is the PDF encrypted?
     **/
    isEncrypted(): PDFPromise<boolean>;

    /**
     * A promise that is resolved with Uint8Array that has the raw PDF data.
     **/
    getData(): PDFPromise<Uint8Array>;

    /**
     * TODO: return type of Promise<???>
     * A promise that is resolved when the document's data is loaded.
     **/
    dataLoaded(): PDFPromise<any[]>;

    /**
     *
     **/
    destroy(): void;
}

interface PDFRef {
    num: number;
    gen: any; // todo
}

interface PDFPageViewportOptions {
    viewBox?: any;
    scale?: number;
    rotation?: number;
    offsetX?: number;
    offsetY?: number;
    dontFlip?: boolean;
}

interface PDFPageViewport {
    width: number;
    height: number;
    fontScale: number;
    transforms: number[];

    clone(options:PDFPageViewportOptions): PDFPageViewport;
    convertToViewportPoint(): number[]; // [x, y]
    convertToViewportRectangle(): number[]; // [x1, y1, x2, y2]
    convertToPdfPoint(): number[]; // [x, y]
}

interface PDFAnnotationData {
    subtype: string;
    rect: number[]; // [x1, y1, x2, y2]
    annotationFlags: any; // todo
    color: number[]; // [r,g,b]
    borderWidth: number;
    hasAppearance: boolean;
}

interface PDFRenderTextLayer {
    beginLayout(): void;
    endLayout(): void;
    appendText(): void;
}

interface PDFRenderImageLayer {
    beginLayout(): void;
    endLayout(): void;
    appendImage(): void;
}

interface PDFRenderParams {
    canvasContext: CanvasRenderingContext2D;
    textLayer?: PDFRenderTextLayer;
    imageLayer?: PDFRenderImageLayer;
    continueCallback?: (_continue:() => void) => void;
}

/**
 * RenderTask is basically a promise but adds a cancel function to termiate it.
 **/
interface PDFRenderTask extends PDFPromise<PDFPageProxy> {

    /**
     * Cancel the rendering task.  If the task is currently rendering it will not be cancelled until graphics pauses with a timeout.  The promise that this object extends will resolve when cancelled.
     **/
    cancel(): void;
}

interface PDFPageProxy {

    /**
     * Page number of the page.  First page is 1.
     **/
    pageNumber: number;

    /**
     * The number of degrees the page is rotated clockwise.
     **/
    rotate: number;

    /**
     * The reference that points to this page.
     **/
    ref: PDFRef;

    /**
     * @return An array of the visible portion of the PDF page in the user space units - [x1, y1, x2, y2].
     **/
    view(): number[];

    getViewport(options:PDFPageViewportOptions): PDFPageViewport;

    /**
     * A promise that is resolved with an array of the annotation objects.
     **/
    getAnnotations(): PDFPromise<PDFAnnotationData[]>;

    /**
     * Begins the process of rendering a page to the desired context.
     * @param params Rendering options.
     * @return An extended promise that is resolved when the page finishes rendering.
     **/
    render(params:PDFRenderParams): PDFRenderTask;

    /**
     * A promise that is resolved with the string that is the text content frm the page.
     **/
    getTextContext(): PDFPromise<string>;

    /**
     * A promise that is resolved with the PDFPageTextData of the Page
     */
    getTextContent(): PDFPromise<PDFPageTextData>;

    /**
     * marked as future feature
     **/
    //getOperationList(): PDFPromise<>;

    /**
     * Destroyes resources allocated by the page.
     **/
    destroy(): void;
}

/**
 * Structure which contains exact informations how to render a Text layer over the canvas
 */
interface PDFPageTextData {
    items: Array<PDFPageTextItem>;
    styles: PDFPageTextStyles;
}

interface PDFPageTextStyles {
    [styleName: string]: PDFPageTextStyle;
}

interface PDFPageTextStyle {
    ascent: number;
    descent: number;

    /**
     * The font-family of the page
     */
    fontFamily: string;

    /**
     * Is the text vertical ?
     */
    vertical?: boolean;
}

interface PDFPageTextItem {
    /**
     * The direction of the Text e.g. ltr
     */
    dir: string;

    /**
     * @see PDFPageTextStyles#styleName
     */
    fontName: string;

    /**
     * The size of the item relative to PDFPageProxy#view
     */
    width: number;
    height: number;

    /**
     * The text string
     */
    str: string;

    transform: PDFTextMatrix;
}

/**
 * This values can be passed to the css transform matrix function
 */
interface PDFTextMatrix {
    0:number; // a
    1:number; // c
    2:number; // b
    3:number; // d
    4:number; // tx
    5:number; // ty
}

/**
 * A PDF document and page is built of many objects.  E.g. there are objects for fonts, images, rendering code and such.  These objects might get processed inside of a worker.  The `PDFObjects` implements some basic functions to manage these objects.
 **/
interface PDFObjects {
    get(objId, callback?): any;
    resolve(objId, data);
    isResolved(objId): boolean;
    hasData(objId): boolean;
    getData(objId): any;
    clear(): void;
}

interface PDFJSStatic {

    workerSrc: string;

    /**
     * The maximum allowed image size in total pixels e.g. width * height.  Images above this value will not be drawn.  Use -1 for no limit.
     **/
    maxImageSize: number;

    /**
     * By default fonts are converted to OpenType fonts and loaded via font face rules.  If disabled, the font will be rendered using a built in font renderer that constructs the glyphs with primitive path commands.
     **/
    disableFontFace: boolean;

    /**
     * This is the main entry point for loading a PDF and interacting with it.
     * NOTE: If a URL is used to fetch the PDF data a standard XMLHttpRequest(XHR)
     * is used, which means it must follow the same origin rules that any XHR does
     * e.g. No corss domain requests without CORS.
     * @param source
     * @param pdfDataRangeTransport Used if you want to manually server range requests for data in the PDF.  @ee viewer.js for an example of pdfDataRangeTransport's interface.
     * @param passwordCallback Used to request a password if wrong or no password was provided.  The callback receives two parameters: function that needs to be called with new password and the reason.
     * @param progressCallback Progress callback.
     * @return A promise that is resolved with PDFDocumentProxy object.
     **/
    getDocument(source:string, pdfDataRangeTransport?, passwordCallback?:(fn:(password:string) => void, reason:string) => string, progressCallback?:(progressData:PDFProgressData) => void)
        : PDFPromise<PDFDocumentProxy>;

    getDocument(source:Uint8Array, pdfDataRangeTransport?, passwordCallback?:(fn:(password:string) => void, reason:string) => string, progressCallback?:(progressData:PDFProgressData) => void)
        : PDFPromise<PDFDocumentProxy>;

    getDocument(source:PDFSource, pdfDataRangeTransport?, passwordCallback?:(fn:(password:string) => void, reason:string) => string, progressCallback?:(progressData:PDFProgressData) => void)
        : PDFPromise<PDFDocumentProxy>;
}

declare var pdfjsLib:PDFJSStatic;

declare module "PDFJS" {
export = pdfjsLib;
}
