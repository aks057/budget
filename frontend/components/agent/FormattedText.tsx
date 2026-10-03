import React from "react";

/**
 * Renders the small markdown subset LLM replies use (paragraphs, "-"/"*"/"1." lists, **bold**) as React nodes.
 * Never uses innerHTML: model output is untrusted text.
 */
export function FormattedText({ text }: { text: string }) {
  const blocks = text.trim().split(/\n{2,}/);
  return (
    <div className="space-y-2 text-sm leading-relaxed">
      {blocks.map((block, index) => (
        <Block key={index} block={block} />
      ))}
    </div>
  );
}

const BULLET = /^\s*[-*•]\s+/;
const NUMBERED = /^\s*\d+[.)]\s+/;

function Block({ block }: { block: string }) {
  const lines = block.split("\n");
  if (lines.every((line) => BULLET.test(line))) {
    return (
      <ul className="list-disc space-y-1 pl-5">
        {lines.map((line, index) => (
          <li key={index}>
            <Inline text={line.replace(BULLET, "")} />
          </li>
        ))}
      </ul>
    );
  }
  if (lines.every((line) => NUMBERED.test(line))) {
    return (
      <ol className="list-decimal space-y-1 pl-5">
        {lines.map((line, index) => (
          <li key={index}>
            <Inline text={line.replace(NUMBERED, "")} />
          </li>
        ))}
      </ol>
    );
  }
  return (
    <p>
      {lines.map((line, index) => (
        <React.Fragment key={index}>
          {index > 0 && <br />}
          <Inline text={line.replace(/^#{1,6}\s+/, "")} />
        </React.Fragment>
      ))}
    </p>
  );
}

function Inline({ text }: { text: string }) {
  const parts = text.split(/(\*\*[^*]+\*\*)/g);
  return (
    <>
      {parts.map((part, index) =>
        part.startsWith("**") && part.endsWith("**") && part.length > 4 ? (
          <strong key={index} className="font-semibold">
            {part.slice(2, -2)}
          </strong>
        ) : (
          <React.Fragment key={index}>{part}</React.Fragment>
        )
      )}
    </>
  );
}
